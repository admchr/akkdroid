package com.akkdroid.client

import akka.actor.{Actor, ActorRef}
import java.net.InetSocketAddress
import akka.io.{Udp, IO}
import android.widget.Toast
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import akka.util.ByteString

class ClientActor extends Actor {      /*
  val broadcast =  new InetSocketAddress("224.0.0.1", 2552)

  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))

  def receive = {
    case Udp.Bound(local) => {
      sender ! Udp.Send(ByteString("oh hello"), broadcast)
    }
    //  context.become(ready(sender))
    // replace receive by ready
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
        socket ! Udp.Send(data, remote) // example server echoes back

    case Udp.Unbind ⇒ socket ! Udp.Unbind
    case Udp.Unbound ⇒ context.stop(self)
  }                                      */
}
