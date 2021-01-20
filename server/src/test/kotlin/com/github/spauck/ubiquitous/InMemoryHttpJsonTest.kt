package com.github.spauck.ubiquitous

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.junit.jupiter.api.Test

class InMemoryHttpJsonTest
{
  private val httpJson = InMemoryHttpJson()

  @Test
  fun `can PUT into a nested path`()
  {
    httpJson.process("PUT", "/nested/path/", """"value"""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path": "value"
          }
        }
      """
    )
  }

  @Test
  fun `can GET from a nested path`()
  {
    httpJson.process("PUT", "/nested/path/", """"value"""")

    val result = httpJson.process("GET", "/nested/path", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(""""value"""")
  }

  @Test
  fun `can PUT an object into a nested path`()
  {
    httpJson.process("PUT", "/nested/path/", """{"field": "value"}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path":
            {
              "field": "value"
            }
          }
        }
      """
    )
  }

  @Test
  fun `PUT strips extra path delimiters`()
  {
    httpJson.process("PUT", "///nested///path///", """"value"""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path": "value"
          }
        }
      """
    )
  }
}
