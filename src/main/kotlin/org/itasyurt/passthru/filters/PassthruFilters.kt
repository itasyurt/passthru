package org.itasyurt.passthru.filters

import com.google.common.io.CharStreams
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.apache.http.HttpRequest
import org.apache.http.entity.InputStreamEntity
import org.apache.http.message.BasicHttpEntityEnclosingRequest

import org.itasyurt.passthru.context.PassThruResponseContext
import org.itasyurt.passthru.context.PassthruContext
import org.itasyurt.passthru.context.PassthruRequestContext
import org.itasyurt.passthru.logger.PassthruLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import java.util.zip.GZIPInputStream
import javax.servlet.http.HttpServletRequest

const val PASSTRHU_CONTEXT = "passthru_context"

@Component
class PassthruSimpleHostRoutingFilter(@Autowired val helper: ProxyRequestHelper,
                                      @Autowired val zuulProperties: ZuulProperties,
                                      @Autowired val httpClientConnectionManagerFactory: ApacheHttpClientConnectionManagerFactory,
                                      @Autowired val httpClientFactory: ApacheHttpClientFactory) : SimpleHostRoutingFilter(helper, zuulProperties, httpClientConnectionManagerFactory, httpClientFactory) {


    override fun buildHttpRequest(verb: String?, uri: String?, entity: InputStreamEntity?, headers: MultiValueMap<String, String>?, params: MultiValueMap<String, String>?, request: HttpServletRequest): HttpRequest {

        return if (verb?.toUpperCase() == "GET") {
            val uriWithQueryString = uri + if (zuulProperties.isForceOriginalQueryStringEncoding) getEncodedQueryString(request) else this.helper.getQueryString(params)
            val result = BasicHttpEntityEnclosingRequest("GET", uriWithQueryString)
            result.entity = entity
            result

        } else {
            super.buildHttpRequest(verb, uri, entity, headers, params, request)
        }


    }

    private fun getEncodedQueryString(request: HttpServletRequest): String? {
        val query = request.queryString
        return if (query != null) "?$query" else ""
    }

}

@Component
class PassthruPreFilter : ZuulFilter() {

    override fun shouldFilter() = true


    override fun filterType() = "pre"

    override fun filterOrder() = 1

    override fun run(): Any {
        val ctx = RequestContext.getCurrentContext()

        val passThru = PassthruContext()
        val requestBody = CharStreams.toString(ctx.request.inputStream.reader())
        val url = ctx.request.requestURL.toString()
        val headers = ctx.request.headerNames.asSequence().map { it -> Pair(it, ctx.request.getHeader(it)) }.toMap()

        passThru.requestContext = PassthruRequestContext(httpMethod = ctx.request.method, url = url, body = requestBody, headers = headers)

        ctx[PASSTRHU_CONTEXT] = passThru
        return Unit
    }


}

@Component
class PassthruPostFilter : ZuulFilter() {

    override fun shouldFilter() = true

    override fun filterType() = "post"

    override fun filterOrder() = 1


    @Autowired
    lateinit var passthruLogger: PassthruLogger

    override fun run(): Any {

        val ctx = RequestContext.getCurrentContext()

        val passThru = ctx[PASSTRHU_CONTEXT] as PassthruContext

        val headers = ctx.response.headerNames.map { it -> Pair(it, ctx.response.getHeader(it)) }.toMap()

        lateinit var responseBody: String
        if (ctx.responseGZipped) {

            val bytes = ctx.responseDataStream.readAllBytes()

            responseBody = CharStreams.toString(GZIPInputStream(bytes.inputStream()).reader())
            // val bos = ByteArrayOutputStream()
            // val gos = GZIPOutputStream(bos)
            // gos.write(responseBody.toByteArray())
            // gos.close()
            ctx.responseDataStream = bytes.inputStream()

        } else {
            if (ctx.responseDataStream != null) {
                responseBody = CharStreams.toString(ctx.responseDataStream.reader())
                ctx.responseBody = responseBody

            } else {
                responseBody = "null"
            }


        }

        passThru.responseContext = PassThruResponseContext(statusCode = ctx.response.status, headers = headers, body = responseBody)

        passthruLogger.log(passThru)


        return Unit
    }


}