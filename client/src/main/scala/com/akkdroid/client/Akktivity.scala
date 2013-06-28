package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{ActorSelection, Props, ActorRef, ActorSystem}
import android.widget._
import java.{lang => jl, util => ju}
import android.content.{Context, Intent}
import android.preference.PreferenceManager
import java.net.NetworkInterface
import scala.collection.JavaConverters._
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import com.akkdroid.util.EnumerationIterator
import android.net.wifi.WifiManager
import android.util.Log

class Akktivity extends Activity {

  import Conversions._

  private var system: ActorSystem = null
  private var serviceURL: String = null
  private var adapter: ArrayAdapter[String] = null
  private var localActor: ActorRef = null
  private var clientActor: ActorRef = null
  private var serverActor: ActorSelection = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var settingsButton: Button = null
  private var messagesList: ListView = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    Log.e("zyx", "ONCREATE")
    setContentView(R.layout.main)
    loadUI()
    initializeMulticast()

    val items = new ju.ArrayList[String]
    adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    messagesList.setAdapter(adapter)

    initializeActors()

    sendButton.onClickAsync { _ =>
      implicit val sender = localActor // impersonate our local actor so it can receive responses from server
      clientActor ! "whateverz"
      //serverActor ! messageTextView.getText.toString

    }
    settingsButton.onClick { _ =>
      startActivity(new Intent(getBaseContext, classOf[AkkdroidPreferences]))
    }
    Log.e("zyx", "!ONCREATE")
  }

  override def onStart() {
    super.onStart()
    Log.e("zyx", "ONSTART")
    initializeActors()
    Log.e("zyx", "!ONSTART")
  }

  override def onResume() {
    super.onResume()
    initializeActors()
  }

  override def onDestroy() {
    system.shutdown()

    super.onDestroy()
  }

  private def loadUI() {
    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    settingsButton = findViewById(R.id.settingsButton).asInstanceOf[Button]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]
  }

  private def loadServiceURL(): String = {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext)
    val ip: String = pref.getString("pref_ip", getString(R.string.pref_ip_value))
    val port: String = pref.getString("pref_port", getString(R.string.pref_port_value))
    s"akka.tcp://server-system@$ip:$port/user/server-actor"
  }

  private def getInetAddress =
    new EnumerationIterator(NetworkInterface.getNetworkInterfaces).asScala.collectFirst {
      case iface if iface.isUp && !iface.isLoopback && iface.getInetAddresses.hasMoreElements =>
        iface.getInetAddresses.nextElement().getHostAddress
    }

  private def initializeMulticast() {
    val wifi = getSystemService(Context.WIFI_SERVICE ).asInstanceOf[WifiManager]
    if(wifi != null){
      val lock = wifi.createMulticastLock("Log_Tag")
      lock.acquire();
    }
  }

  private def initializeActors() {
    if (system == null) {
      val inetAddress = getInetAddress.getOrElse(throw new Exception("No public IP address found!"))
      val config = ConfigFactory.load().withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(inetAddress))

      system = ActorSystem("mobile-system", config)

      // all messages received by local actor will be passed to handler and handled with code below
      val handler = new Handler
      def newLocalActor = new HandlerDispatcherActor(handler, msg => {
        adapter.add(msg.toString)
        adapter.notifyDataSetChanged()
      })
      localActor = system.actorOf(Props(newLocalActor), name = "mobile-actor")
      clientActor = system.actorOf(Props[ClientActor], name = "client-actor")
    }

    val newServiceURL = loadServiceURL()
    if (newServiceURL != serviceURL) {
      serviceURL = newServiceURL
      serverActor = system.actorSelection(serviceURL)
    }
  }
}
