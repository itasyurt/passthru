package org.itasyurt.passthru.context

class PassthruContext {

    lateinit var requestContext: PassthruRequestContext

    lateinit var responseContext: PassThruResponseContext

}
data class PassthruRequestContext(val httpMethod: String, val url:String, val headers:Map<String,String>, val body: String)
data class PassThruResponseContext(val statusCode: Int, val headers: Map<String, String>, val body: String)