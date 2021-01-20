package com.github.spauck.ubiquitous

interface HttpJson
{
  fun process(
    method: String,
    path: String,
    json: String,
  ): HttpResult
}
