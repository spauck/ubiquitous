package com.github.spauck.ubiquitous

import com.github.spauck.ubiquitous.client.sendHttp
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class HttpSendTest
{
  companion object
  {
    private lateinit var server: Server

    @JvmStatic
    @BeforeAll
    fun setup()
    {
      server = Server(InMemoryHttpJson()).host(port = 9999)
      // Wait for server to start
      while (true)
      {
        try
        {
          sendHttp("PUT", "http://localhost:9999", "something")
          break
        }
        finally
        {
          println("Waiting for server to start...")
          Thread.sleep(1000)
        }
      }
    }

    @JvmStatic
    @AfterAll
    fun teardown()
    {
      server.shutdown()
    }
  }

  @Test
  fun `The client server combination and PUT data and GET part of it back`()
  {
    sendHttp("PUT", "http://localhost:9999/test", """{ "thing": "thong" }""")

    val result = sendHttp("GET", "http://localhost:9999/test/thing", null)

    assertThatJson(result).isEqualTo("thong")
  }
}
