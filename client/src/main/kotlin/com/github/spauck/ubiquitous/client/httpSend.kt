package com.github.spauck.ubiquitous.client

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors

fun send(
  method: String,
  url: String,
  requestBody: String? = null,
  headers: Map<String, String> = mapOf(
    "Content-Type" to "application/json",
    "Accept" to "application/json"),
): HttpResult
{

  var resultBody: String
  lateinit var connection: HttpURLConnection
  with(URL(url).openConnection() as HttpURLConnection) {
    connection = this
    requestMethod = method
    headers.forEach { (name, value) -> addRequestProperty(name, value) }
    if (requestBody != null)
    {
      doOutput = true
      val writer = OutputStreamWriter(outputStream)
      writer.write(requestBody)
      writer.flush()
    }
    else
    {
      // Calling .getResponseCode() triggers the call in the case without a request body.
      responseCode
    }

    (errorStream ?: inputStream).bufferedReader().use {
      resultBody = it.lines().collect(Collectors.joining())
    }
  }

  return HttpResult(connection.responseCode, resultBody, connection.headerFields)
}

data class HttpResult(
  val status: Int,
  val body: String,
  val headers: Map<String, List<String>>,
)
