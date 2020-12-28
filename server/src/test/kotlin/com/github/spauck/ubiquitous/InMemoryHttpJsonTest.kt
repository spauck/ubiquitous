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
  fun `can PUT into a root path`()
  {
    httpJson.process("PUT", "/", """"value"""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(""""value"""")
  }

  @Test
  fun `can GET from a nested path`()
  {
    httpJson.process("PUT", "/nested/path/", """"value"""")

    val result = httpJson.process("GET", "/nested/path", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(""""value"""")
  }

  @Test
  fun `can GET from an array path`()
  {
    httpJson.process("PUT", "/path/", """["zero", "one"]""")

    val result = httpJson.process("GET", "/path:1", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(""""one"""")
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
  fun `can PUT an object into a path containing an array index`()
  {
    httpJson.process("PUT", "/nested:0/path/", """{"field": "value"}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          [
            {
              "path":
              {
                "field": "value"
              }
            }
          ]
        }
      """
    )
  }

  /**
   * IMPORTANT: This has the limitation that empty string keys are not acceptable!
   */
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

  /**
   * IMPORTANT: This has the limitation that empty string keys are not acceptable!
   */
  @Test
  fun `GET strips extra path delimiters`()
  {
    httpJson.process("PUT", "/nested/path/another", """"value"""")

    val result = httpJson.process("GET", "///nested///path///", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "another": "value"
        }
      """
    )
  }

  @Test
  fun `can PATCH at root level`()
  {
    httpJson.process("PATCH", "/", """{"field": "value"}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo("""{"field": "value"}""")
  }

  @Test
  fun `can PATCH an object to add a field`()
  {
    httpJson.process("PUT", "/nested/path/", """{"field": "value"}""")
    httpJson.process("PATCH", "nested/", """{"field2": "value2"}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path":
            {
              "field": "value"
            },
            "field2": "value2"
          }
        }
      """
    )
  }

  @Test
  fun `can PATCH an object to null a field`()
  {
    httpJson.process("PUT", "/nested/path/", """{"field": "value"}""")
    httpJson.process("PATCH", "/", """{"nested": {"path": null}}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path": null
          }
        }
      """
    )
  }

  @Test
  fun `can PATCH an object to add a nested field`()
  {
    httpJson.process("PUT", "/nested/path/", """{"field": "value"}""")
    httpJson.process("PATCH", "nested/", """{"path": {"field2": "value2"}}""")

    val result = httpJson.process("GET", "/", "")

    JsonAssertions.assertThatJson(result.body).isEqualTo(
      """
        {
          "nested":
          {
            "path":
            {
              "field": "value",
              "field2": "value2"
            }
          }
        }
      """
    )
  }

  @Test
  fun `cannot PATCH an object field to a different type`()
  {
    httpJson.process("PUT", "/", """{"field": "string"}""")

    val patchResult = httpJson.process("PATCH", "/", """{"field": 123}""")
    JsonAssertions.assertThatJson(patchResult.statusCode).isEqualTo(409)
  }

  // The object layering helps to guarantee it isn't a false pass
  // that might occur due to field ordering at a single layer.
  @Test
  fun `a failed PATCH should not modify the data`()
  {
    val intialJson = """
        {
          "layer1":
          {
            "field": "string",
            "layer2":
            {
              "field": "string",
              "layer3":
              {
                "field": "string"
              }
            }
          }
        }
      """
    httpJson.process("PUT", "/", intialJson)

    val patchResult = httpJson.process(
      "PATCH", "/",
      """
        {
          "layer1":
          {
            "field": "NEW string",
            "layer2":
            {
              "field": {"should": "fail"},
              "layer3":
              {
                "field": "NEW string"
              }
            }
          }
        }
      """
    )
    JsonAssertions.assertThatJson(patchResult.statusCode).isEqualTo(409)

    // This is the assertion we care most about
    val getResult = httpJson.process("GET", "/", "")
    JsonAssertions.assertThatJson(getResult.body).isEqualTo(intialJson)
  }
}
