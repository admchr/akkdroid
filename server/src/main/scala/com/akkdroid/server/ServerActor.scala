package com.akkdroid.server

import akka.actor.{ActorRef, RootActorPath, Actor}
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import java.net.InetSocketAddress
import akka.io.{Udp, IO}

class ServerActor(listenAddress:String, listenPort:Int) extends Actor {
/*  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)
  */
  val broadcast =  new InetSocketAddress("224.0.0.1", 2552)

  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(listenAddress, 2553))

  def receive = {
    case Udp.Bound(local) =>
      context.become(ready(sender))
    // replace receive by ready
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      socket ! Udp.Send(data, remote) // example server echoes back

    case Udp.Unbind ⇒ socket ! Udp.Unbind
    case Udp.Unbound ⇒ context.stop(self)

    case msg:String =>
      println(s"$sender says: $msg")
      sender ! s"You said: $msg"
  }
}
