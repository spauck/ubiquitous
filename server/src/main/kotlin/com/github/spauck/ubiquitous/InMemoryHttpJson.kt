package com.github.spauck.ubiquitous

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

class InMemoryHttpJson : HttpJson
{
  private val store = mutableMapOf<String, Any?>()

  override fun process(method: String, path: String, json: String): HttpResult
  {
    return when (supported(method))
    {
      SupportedMethods.GET -> get(path)
      SupportedMethods.PUT -> put(path, json)
      SupportedMethods.PATCH -> patch(path, json)
      null -> HttpResult(
        405,
        "Method Not Allowed",
        mapOf("Allow" to listOf(SupportedMethods.values().joinToString(", ")))
      )
    }
  }

  private fun get(path: String): HttpResult
  {
    return HttpResult(
      200,
      ObjectMapper().writeValueAsString(getNested(store, split(path))),
    )
  }

  private fun getNested(
    map: Any?,
    path: Deque<String>): Any?
  {
    if (path.size == 0)
    {
      return map
    }
    else
    {
      if (map !is Map<*, *>)
      {
        // Should send 404 or something, depending on type
        throw IllegalAccessError()
      }
      else
      {
        val first = path.removeFirst()
        return getNested(map[first], path)
      }
    }
  }

  private fun put(path: String, json: String): HttpResult
  {
    val data = ObjectMapper().readValue(json, Any::class.java)
    putNested(store, split(path), data)
    return HttpResult(200, "")
  }

  private fun patch(path: String, json: String): HttpResult
  {
    val data = ObjectMapper().readValue(json, LinkedHashMap::class.java)
    putNested(store, split(path), data)
    return HttpResult(200, "")
  }

  private fun split(path: String) = ArrayDeque(path.split('/').filter { it.isNotEmpty() })

  private fun putNested(
    map: MutableMap<String, Any?>,
    path: Deque<String>,
    json: Any?)
  {
    if (path.size == 1)
    {
      map[path.first] = json
    }
    else
    {
      val first = path.removeFirst()
      var nested = map[first]
      if (nested !is Map<*, *>)
      {
        nested = mutableMapOf<String, Any?>()
        map[first] = nested
      }

      @Suppress("UNCHECKED_CAST")
      putNested(
        nested as MutableMap<String, Any?>,
        path,
        json
      )
    }
  }
}
