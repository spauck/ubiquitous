package com.github.spauck.ubiquitous

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

internal class Server(private val httpJson: HttpJson)
{
  private var httpServer: HttpServer? = null

  internal fun host(port: Int): Server
  {
    val server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)
    server.createContext("/", Handler(httpJson))
    server.executor = Executors.newFixedThreadPool(10)
    server.start()
    this.httpServer = server
    return this
  }

  internal fun shutdown()
  {
    httpServer?.stop(5)
  }

  private class Handler(private val httpJson: HttpJson) : HttpHandler
  {
    override fun handle(exchange: HttpExchange?)
    {
      try
      {
        checkNotNull(exchange)

        val result = httpJson.process(
          exchange.requestMethod,
          exchange.requestURI.toString(),
          exchange.requestBody.readBytes().decodeToString()
        )

        val body = result.body.toByteArray()
        val contentLength = body.size
        exchange.sendResponseHeaders(result.statusCode, contentLength.toLong())
        exchange.responseHeaders.putAll(result.headers)

        val outputStream = exchange.responseBody
        outputStream.write(body)
        outputStream.flush()
        outputStream.close()
      }
      catch (ex: Exception)
      {
        println(ex.printStackTrace())
      }
    }
  }
}
