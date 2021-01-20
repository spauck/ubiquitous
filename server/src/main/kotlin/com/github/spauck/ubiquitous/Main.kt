package com.github.spauck.ubiquitous

fun main()
{
  val server = Server(InMemoryHttpJson()).host(port = 9180)
  onShutdown(server::shutdown)
}

private fun onShutdown(run: () -> Unit)
{
  Runtime.getRuntime().addShutdownHook(object : Thread()
  {
    override fun run() = run()
  })
}
