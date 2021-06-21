package com.github.spauck.ubiquitous.client

import java.io.OutputStreamWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors

@JvmOverloads
fun sendHttp(
  method: String,
  url: String,
  requestBody: String? = null,
  headers: Map<String, String> = mapOf(),
): HttpResult
{
  var resultBody: String? = null
  lateinit var connection: HttpURLConnection
  with(URL(url).openConnection() as HttpURLConnection) {
    connection = this
    requestMethod = method
    headers.forEach(::addRequestProperty)
    if (requestBody != null)
    {
      if (!headers.containsKey("Content-Type"))
      {
        addRequestProperty("Content-Type", "")
      }
      doOutput = true
      val writer = OutputStreamWriter(outputStream)
      writer.write(requestBody)
      writer.flush()
    }

    // Calling .getResponseCode() triggers the call in the case without a request body.
    responseCode

    getPossibleStream(responseCode)?.bufferedReader()?.use {
      resultBody = it.lines().collect(Collectors.joining())
    }
  }

  return HttpResult(connection.responseCode, resultBody, connection.headerFields)
}

/**
 * This is to work around limitations in the Java HttpConnection implementation.
 * From: https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
 */
fun allowExtraHttpMethods(vararg methods: String)
{
  val methodsField = HttpURLConnection::class.java.getDeclaredField("methods")
  val modifiersField = Field::class.java.getDeclaredField("modifiers")
  modifiersField.isAccessible = true
  modifiersField.setInt(methodsField, methodsField.modifiers and Modifier.FINAL.inv())
  methodsField.isAccessible = true
  val oldMethods = methodsField.get(null) as Array<String>
  val methodsSet: MutableSet<String> = LinkedHashSet(listOf(*oldMethods))
  methodsSet.addAll(listOf(*methods))
  val newMethods = methodsSet.toTypedArray()
  methodsField.set(null, newMethods)
}

private fun HttpURLConnection.getPossibleStream(responseCode: Int) =
  errorStream ?: if (responseCode in 200..399) inputStream else null

data class HttpResult(
  val statusCode: Int,
  val body: String?,
  val headers: Map<String, List<String>>,
)
