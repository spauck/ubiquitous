package com.github.spauck.ubiquitous.client

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors

fun send(
  method: String,
  path: String,
  requestBody: String?,
): String
{
  val url = URL(path)

  var result: String

  with(url.openConnection() as HttpURLConnection) {
    requestMethod = method

    if (requestBody != null)
    {
      doOutput = true
      val writer = OutputStreamWriter(outputStream)
      writer.write(requestBody)
      writer.flush()
    }
    else
    {
      // Calling .getResponseCode() triggers the call.
      responseCode
    }

    println("\nSent '$requestMethod' request to URL : $url; Response Code : $responseCode")

    if (responseCode >= 400)
    {
      result = ""
    }
    else
    {
      inputStream.bufferedReader().use {
        result = it.lines().collect(Collectors.joining())
        println(result)
      }
    }
  }

  return result
}
