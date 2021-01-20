package com.github.spauck.ubiquitous

data class HttpResult(
  val statusCode: Int,
  val body: String,
  val headers: Map<String, List<String>> = mapOf()
)
