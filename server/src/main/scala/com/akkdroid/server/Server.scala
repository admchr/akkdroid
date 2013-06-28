package com.akkdroid.server

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import java.net.{DatagramPacket, MulticastSocket, InetAddress}

/**
 * Created with IntelliJ IDEA.
 * User: ghik
 * Date: 28.04.13
 * Time: 13:11
 */
object Server {
  def main(args: Array[String]) {
    var config = ConfigFactory.load()
    if (args.length > 0) {
      config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(args(0)))
    }
    val system = ActorSystem("server-system", config)
    system.eventStream.setLogLevel(Logging.DebugLevel)

    val actor = system.actorOf(Props[ServerActor], "server-actor")
    mcast()
  }
  def mcast() {
    val msg = "Hello"
    val group = InetAddress.getByName("224.0.0.1")
    val s = new MulticastSocket(2552)
    s.joinGroup(group)
    val hi = new DatagramPacket(msg.getBytes(), msg.length(),
      group, 2552)
    s.send(hi)
    /*
 // get their responses!
 byte[] buf = new byte[1000];
 DatagramPacket recv = new DatagramPacket(buf, buf.length);
 s.receive(recv);
 ...
 // OK, I'm done talking - leave the group...
 s.leaveGroup(group);
    * */
  }
}
