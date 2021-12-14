package com.nozomi.shttpa

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class WebServer(private val context: Context, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val ct = ContentType(session.headers["content-type"]).tryUTF8()
        session.headers["content-type"] = ct.contentTypeHeader

        if (session.method == Method.POST) {
            val name = "file"
            val body = mutableMapOf<String, String>()
            session.parseBody(body)
            val filePath = body[name]

            if (filePath != null) {
                val filename = session.parameters[name]?.first() ?: "UnknownName"
                val data = File(filePath).readBytes()
                saveToDownloadFolder(filename, data)
            }
        }

        return responseFileFromAssets(session.uri)
    }

    @SuppressLint("InlinedApi")
    private fun saveToDownloadFolder(filename: String, data: ByteArray) {
        val epUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
        }
        val contentUri = context.contentResolver.insert(epUri, values)

        context.contentResolver.openFileDescriptor(contentUri!!, "w", null).use {
            FileOutputStream(it!!.fileDescriptor).use { output ->
                output.write(data)
            }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        context.contentResolver.update(contentUri, values, null, null)
    }

    private fun responseFileFromAssets(uri: String): Response {
        try {
            var filename = uri

            if ("/" == uri) {
                filename = "index.html"
            } else if (uri.substring(0, 1) == "/") {
                filename = uri.substring(1)
            }
            Log.d("WebServer", filename)

            val assets = context.resources.assets
            var file: InputStream? = null
            try {
                file = assets.open(filename)
            } catch (e: Exception) {
                Log.d("WebServer", "File open failed")
            }

            if (filename.endsWith(".ico")) {
                return newChunkedResponse(Response.Status.OK, "image/x-icon", file)
            } else if (filename.endsWith(".png") || filename.endsWith(".PNG")) {
                return newChunkedResponse(Response.Status.OK, "image/png", file)
            } else if (filename.endsWith(".jpg") || filename.endsWith(".JPG")
                || filename.endsWith(".jpeg") || filename.endsWith(".JPEG")
            ) {
                return newChunkedResponse(Response.Status.OK, "image/jpeg", file)
            } else if (filename.endsWith(".js")) {
                return newChunkedResponse(Response.Status.OK, "application/javascript", file)
            } else if (filename.endsWith(".css")) {
                return newChunkedResponse(Response.Status.OK, "text/css", file)
            } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                Log.d("WebServer", "html")
                return newChunkedResponse(Response.Status.OK, "text/html", file)
            } else if (filename.endsWith(".map")) {
                return newChunkedResponse(Response.Status.OK, "application/json", file)
            } else if (filename.endsWith(".svg")) {
                return newChunkedResponse(Response.Status.OK, "image/svg+xml", file)
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", filename)
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
