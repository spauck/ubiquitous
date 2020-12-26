package com.github.spauck.ubiquitous

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URLDecoder

class InMemoryHttpJson : HttpJson
{
  private val objectMapper = ObjectMapper()

  private var store: Any? = null

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
    val (found, value) = getNested(store, split(path))
    return if (found)
    {
      HttpResult(
        200,
        objectMapper.writeValueAsString(value),
      )
    }
    else
    {
      HttpResult(
        404,
        "Not Found",
      )
    }
  }

  private fun getNested(
    jsonStructure: Any?,
    path: Deque<StructureKey>): (GetResult)
  {
    if (path.size == 0)
    {
      return GetResult(true, jsonStructure)
    }
    else
    {
      val key = path.removeFirst()
      if (jsonStructure is Map<*, *> && key is ObjectKey)
      {
        val map = jsonStructure as MutableMap<String, Any?>
        return getNested(map[key.field], path)
      }
      else if (jsonStructure is List<*> && key is ArrayKey)
      {
        return if (jsonStructure.size <= key.index + 1)
        {
          getNested(jsonStructure[key.index], path)
        }
        else
        {
          GetResult(false, null)
        }
      }
      else
      {
        return GetResult(false, null)
      }
    }
  }

  private fun put(path: String, json: String): HttpResult
  {
    val data = objectMapper.readValue(json, Any::class.java)
    return if (putNested(ArbitraryAccessor({ store }, ::setStore), split(path), data))
    {
      HttpResult(200, "")
    }
    else
    {
      HttpResult(409, "Conflict")
    }
  }

  private fun patch(path: String, json: String): HttpResult
  {
    val data = objectMapper.readValue(json, Any::class.java)
    return if (patchNested(ArbitraryAccessor({ store }, ::setStore), split(path), data))
    {
      HttpResult(200, "")
    }
    else
    {
      HttpResult(409, "Conflict")
    }
  }

  private fun setStore(v: Any?)
  {
    store = v
  }

  @Throws(NumberFormatException::class)
  private fun split(path: String) = ArrayDeque(path.split('/')
    .filter { it.isNotEmpty() }
    .flatMap {
      it
        .split(':')
        .mapIndexed { i, v ->
          if (i == 0)
          {
            ObjectKey(URLDecoder.decode(v, "UTF-8"))
          }
          else
          {
            ArrayKey(v.toInt())
          }
        }
    })

  private fun putNested(
    accessor: Accessor,
    path: Deque<StructureKey>,
    json: Any?): Boolean
  {
    if (path.size == 0)
    {
      accessor.set(json)
      return true
    }
    else
    {
      val key = path.removeFirst()
      when (key)
      {
        is ObjectKey ->
        {
          val field: String = key.field
          val jsonStructure = getAndSetIfNull(accessor) { mutableMapOf<String, Any?>() }
          return when (jsonStructure)
          {
            is MutableMap<*, *> ->
            {
              val map = jsonStructure as MutableMap<String, Any?>
              putNested(
                MapAccessor(map, field),
                path,
                json,
              )
            }
            else -> false
          }
        }
        is ArrayKey ->
        {
          val index: Int = key.index
          val jsonStructure = getAndSetIfNull(accessor) { mutableListOf<Any?>() }
          when (jsonStructure)
          {
            is MutableList<*> ->
            {
              val list = jsonStructure as MutableList<Any?>

              return when
              {
                index > list.size ->
                {
                  false
                }
                else ->
                {
                  putNested(
                    ListAccessor(list, index),
                    path,
                    json,
                  )
                }
              }
            }
            else -> return false
          }
        }
      }
    }
  }

  private fun patchNested(
    accessor: Accessor,
    path: Deque<StructureKey>,
    json: Any?): Boolean
  {
    if (path.size == 0)
    {
      accessor.set(json)
      return true
    }
    else
    {
      val key = path.removeFirst()
      when (key)
      {
        is ObjectKey ->
        {
          val field: String = key.field
          val jsonStructure = getAndSetIfNull(accessor) { mutableMapOf<String, Any?>() }
          return when (jsonStructure)
          {
            is MutableMap<*, *> ->
            {
              val map = jsonStructure as MutableMap<String, Any?>
              putNested(
                MapAccessor(map, field),
                path,
                json,
              )
            }
            else -> false
          }
        }
        is ArrayKey ->
        {
          val index: Int = key.index
          val jsonStructure = getAndSetIfNull(accessor) { mutableListOf<Any?>() }
          when (jsonStructure)
          {
            is MutableList<*> ->
            {
              val list = jsonStructure as MutableList<Any?>

              return when
              {
                index > list.size ->
                {
                  false
                }
                else ->
                {
                  putNested(
                    ListAccessor(list, index),
                    path,
                    json,
                  )
                }
              }
            }
            else -> return false
          }
        }
      }
    }
  }

  private inline fun getAndSetIfNull(
    accessor: Accessor,
    ifNull: () -> Any,
  ): Any
  {
    var value = accessor.get()
    if (value == null)
    {
      value = ifNull()
      accessor.set(value)
    }
    return value
  }
}

data class GetResult(
  val found: Boolean,
  val value: Any?,
)

sealed class StructureKey

data class ObjectKey(val field: String) : StructureKey()
data class ArrayKey(val index: Int) : StructureKey()

interface Accessor
{
  fun get(): Any?
  fun set(value: Any?)
}

class ArbitraryAccessor(
  private val getter: () -> Any?,
  private val setter: (Any?) -> Unit,
) : Accessor
{
  override fun get() = getter()

  override fun set(value: Any?)
  {
    setter(value)
  }
}

class MapAccessor<T>(
  private val map: MutableMap<T, Any?>,
  private val key: T,
) : Accessor
{
  override fun get() = map[key]

  override fun set(value: Any?)
  {
    map[key] = value
  }
}

class ListAccessor(
  private val list: MutableList<Any?>,
  private val index: Int,
) : Accessor
{
  override fun get() = list.getOrNull(index)

  override fun set(value: Any?)
  {
    when
    {
      index == list.size -> list.add(index, value)
      index < list.size -> list[index] = value
      else -> throw IndexOutOfBoundsException()
    }
  }
}
