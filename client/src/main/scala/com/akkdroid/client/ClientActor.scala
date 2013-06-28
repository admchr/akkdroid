package com.akkdroid.client

import akka.actor.{Actor, ActorRef}
import java.net.{DatagramPacket, MulticastSocket, InetAddress, InetSocketAddress}
import akka.io.{Udp, IO}
import android.widget.Toast
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import akka.util.ByteString
import android.util.Log

class ClientActor extends Actor {
val addr = "192.168.43.124"
val braddr = "224.0.0.1"
val anyaddr = "0.0.0.0"
val broadcast = new InetSocketAddress(braddr, 2552)
val someone = new InetSocketAddress(addr, 2552)
var sock :ActorRef = null
import context.system
//IO(Udp) ! Udp.Bind(self, new InetSocketAddress(anyaddr, 2552))

def receive = {
case Udp.Bound(local) => {
//sender ! Udp.Send(ByteString("oh hello"), someone)
sock = sender
  Log.e("zyx", "got binded" + local + " " + sender)
}

case msg:String => {
  Log.e("zyx", "got beg"+msg)
  //sock ! Udp.Send(ByteString("ohz"), broadcast)
  Log.e("zyx", "got med"+msg)
  mcast()
  Log.e("zyx", "got end"+msg)
}
case x => {
  Log.e("zyx", "unknown msg!")
}
// context.become(ready(sender))
// replace receive by ready
}

def ready(socket: ActorRef): Receive = {
case Udp.Received(data, remote) =>
socket ! Udp.Send(data, remote) // example server echoes back

case Udp.Unbind ⇒ socket ! Udp.Unbind
case Udp.Unbound ⇒ context.stop(self)

}

def mcast() {
  try {
  val msg = "Hellos"
  Log.e("zyx", "got med0")
  val group = InetAddress.getByName("224.0.0.123")
  Log.e("zyx", "got med1")
  val s = new MulticastSocket();//2552
  Log.e("zyx", "got med2")
  s.joinGroup(group)
  Log.e("zyx", "got med3")
  val hi = new DatagramPacket(msg.getBytes(), msg.length(),
    group, 2552)
  s.send(hi)
  } catch {case e : Throwable => Log.e("zyx", "an exception "+e+ " occured " + e.getStackTrace().map(s=>s.toString).reduceLeft((s, k) => s+"\n"+k))
  }
}

}
