package com.akkdroid.server

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}

/**
 * Created with IntelliJ IDEA.
 * User: ghik
 * Date: 28.04.13
 * Time: 13:11
 */
object Server {

  val hostnameKey = "akka.remote.netty.hostname"
  val portKey = "akka.remote.netty.port"

  def main(args: Array[String]) {
    var config = ConfigFactory.load()
    if (args.length > 0) {
      val hostname = args(0)
      config = config.withValue(hostnameKey, ConfigValueFactory.fromAnyRef(hostname))
    }
    val hostname = config.getString(hostnameKey)
    val port = config.getInt(portKey)

    val system = ActorSystem("server-system", config)
    system.eventStream.setLogLevel(Logging.DebugLevel)

    val actor = system.actorOf(Props(new ServerActor(hostname, port)), "server-actor")
  }
}
