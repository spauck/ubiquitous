package com.github.spauck.ubiquitous

enum class SupportedMethods
{
  GET,
  PATCH,
  PUT,
  POST,
  ;
}

private val lookup = SupportedMethods.values().associateBy { it.name }

fun supported(method: String) = lookup[method.trim().toUpperCase()]
