package com.github.spauck.ubiquitous.client

import com.github.spauck.ubiquitous.InMemoryHttpJson
import com.github.spauck.ubiquitous.Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class HttpSendKtTest
{
  private lateinit var server: Server

  @BeforeAll
  fun setup()
  {
    server = Server(InMemoryHttpJson()).host(port = 9999)
  }

  @AfterAll
  fun teardown()
  {
    server.shutdown()
  }

  @Test
  fun `it works`()
  {
    send("PUT", "http://localhost:9999/test", """{ "thing": "thong" }""")

    val result = send("GET", "http://localhost:9999/test/thing", null)

    assertThat(result).isEqualTo("thong")
  }
}