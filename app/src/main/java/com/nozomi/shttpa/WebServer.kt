package com.nozomi.shttpa

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream

class WebServer(private val context: Context, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        try {
            var uri = session.uri
            if ("/" == uri) {
                uri = "index.html"
            }

            var filename = uri
            if (uri.substring(0, 1) == "/") {
                filename = filename.substring(1)
            }
            Log.d("WebServer", filename)

            val assets = context.resources.assets
            var file: InputStream? = null
            try {
                file = assets.open(filename)
            } catch (e: Exception) {
                Log.d("WebServer", "File open failed")
            }

            if (uri.endsWith(".ico")) {
                return newChunkedResponse(Response.Status.OK, "image/x-icon", file)
            } else if (uri.endsWith(".png") || uri.endsWith(".PNG")) {
                return newChunkedResponse(Response.Status.OK, "image/png", file)
            } else if (uri.endsWith(".jpg") || uri.endsWith(".JPG")
                || uri.endsWith(".jpeg") || uri.endsWith(".JPEG")
            ) {
                return newChunkedResponse(Response.Status.OK, "image/jpeg", file)
            } else if (uri.endsWith(".js")) {
                return newChunkedResponse(Response.Status.OK, "application/javascript", file)
            } else if (uri.endsWith(".css")) {
                return newChunkedResponse(Response.Status.OK, "text/css", file)
            } else if (uri.endsWith(".html") || uri.endsWith(".htm")) {
                return newChunkedResponse(Response.Status.OK, "text/html", file)
            } else if (uri.endsWith(".map")) {
                return newChunkedResponse(Response.Status.OK, "application/json", file)
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", uri)
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/plain",
                ioe.localizedMessage
            )
        }
    }
}
