package org.itasyurt.passthru.logger

import org.itasyurt.passthru.context.PassThruResponseContext
import org.itasyurt.passthru.context.PassthruContext
import org.itasyurt.passthru.context.PassthruRequestContext
import java.util.logging.Logger

interface PassthruLogger {

    fun log(context: PassthruContext)

}


class ConsoleLogger : PassthruLogger {


    override fun log(context: PassthruContext) {
        val buffer = StringBuilder()
        buffer.appendln("---------------------")
        logRequestInfo(buffer, context.requestContext)
        logResponseInfo(buffer, context.responseContext)
        buffer.appendln("---------------------")
        logger.info(buffer.toString())
    }

    private fun logResponseInfo(buffer: StringBuilder, resp: PassThruResponseContext) {
        buffer.appendln("Response ${resp.statusCode}")
        buffer.appendln("Response Headers: ${resp.headers}")
        buffer.append(resp.body)
        buffer.appendln()
    }

    private fun logRequestInfo(buffer: StringBuilder, req: PassthruRequestContext) {
        buffer.appendln("${req.httpMethod} ${req.url}")
        buffer.appendln("Request Headers: ${req.headers}")
        buffer.append(req.body)
        buffer.appendln()
    }

    companion object Logger {
        val logger = java.util.logging.Logger.getLogger(this.javaClass.name)!!

    }

}