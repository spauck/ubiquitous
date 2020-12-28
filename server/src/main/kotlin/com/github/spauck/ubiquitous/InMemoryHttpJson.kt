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
    val (found, value) = getNested(store, split(path), 0)
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
    path: List<StructureKey>,
    index: Int,
  ): GetResult
  {
    if (path.size == index)
    {
      return GetResult(true, jsonStructure)
    }
    else
    {
      val key = path[index]
      if (jsonStructure is Map<*, *> && key is ObjectKey)
      {
        val map = jsonStructure as MutableMap<String, Any?>
        return getNested(map[key.field], path, index + 1)
      }
      else if (jsonStructure is List<*> && key is ArrayKey)
      {
        return if (jsonStructure.size <= key.index + 1)
        {
          getNested(jsonStructure[key.index], path, index + 1)
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
    val accessor = nestedAccessorIn(ArbitraryAccessor({ store }, ::setStore), split(path), 0)
    return if (accessor != null)
    {
      val existing = accessor.get()
      accessor.set(data)

      if (existing == null)
      {
        HttpResult(201, "")
      }
      else
      {
        HttpResult(204, "")
      }
    }
    else
    {
      HttpResult(409, "Conflict")
    }
  }

  private fun patch(path: String, json: String): HttpResult
  {
    val data = objectMapper.readValue(json, Any::class.java)
    val accessor = nestedAccessorIn(ArbitraryAccessor({ store }, ::setStore), split(path), 0)
    return if (accessor != null)
    {
      val patchApplications = patchNested(accessor, data)
      if (patchApplications != null)
      {
        patchApplications.forEach { it.apply() }
        HttpResult(200, objectMapper.writeValueAsString(accessor.get()))
      }
      else
      {
        HttpResult(409, "Conflict")
      }
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
  private fun split(path: String) = path.split('/')
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
    }

  private fun nestedAccessorIn(
    accessor: Accessor,
    path: List<StructureKey>,
    pathIndex: Int,
  ): Accessor?
  {
    if (path.size == pathIndex)
    {
      return accessor
    }
    else
    {
      when (val key = path[pathIndex])
      {
        is ObjectKey ->
        {
          val field: String = key.field
          val jsonStructure = accessor.getAndSetIfNull { mutableMapOf<String, Any?>() }
          return when (jsonStructure)
          {
            is MutableMap<*, *> ->
            {
              val map = jsonStructure as MutableMap<String, Any?>
              nestedAccessorIn(MapAccessor(map, field), path, pathIndex + 1)
            }
            else -> null
          }
        }
        is ArrayKey ->
        {
          val jsonStructure = accessor.getAndSetIfNull { mutableListOf<String>() }
          return when (jsonStructure)
          {
            is MutableList<*> ->
            {
              val list = jsonStructure as MutableList<Any?>

              when
              {
                key.index > list.size -> null
                else -> nestedAccessorIn(ListAccessor(list, key.index), path, pathIndex + 1)
              }
            }
            else -> null
          }
        }
      }
    }
  }

  private fun patchNested(
    accessor: Accessor,
    json: Any?,
  ): List<PatchApplication>?
  {
    val stored = accessor.get()
    return when
    {
      stored == null || json == null -> listOf(PatchApplication(accessor, json))
      stored::class == json::class ->
      {
        return when (json)
        {
          is Map<*, *> ->
          {
            val results = mutableListOf<PatchApplication>()
            for ((key, value) in json)
            {
              val map = stored as MutableMap<String, Any?>
              val newAcc = MapAccessor(map, key as String)
              val patchApplications = patchNested(newAcc, value)
              if (patchApplications != null)
              {
                results.addAll(patchApplications)
              }
              else
              {
                return null
              }
            }
            results
          }
          is List<*> ->
          {
            val results = mutableListOf<PatchApplication>()
            for ((index, value) in json.withIndex())
            {
              val list = stored as MutableList<Any?>
              val newAcc = ListAccessor(list, index)
              val patchApplications = patchNested(newAcc, value)
              if (patchApplications != null)
              {
                results.addAll(patchApplications)
              }
              else
              {
                return null
              }
            }
            results
          }
          else ->
          {
            listOf(PatchApplication(accessor, json))
          }
        }
      }
      else -> null
    }
  }
}

data class GetResult(
  val found: Boolean,
  val value: Any?,
)

data class PatchApplication(
  private val accessor: Accessor,
  private val setValue: Any?
)
{
  fun apply()
  {
    accessor.set(setValue)
  }
}

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

class MapAccessor(
  private val map: MutableMap<String, Any?>,
  private val key: String,
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

private inline fun Accessor.getAndSetIfNull(
  ifNull: () -> Any,
): Any
{
  var value = this.get()
  if (value == null)
  {
    value = ifNull()
    this.set(value)
  }
  return value
}
