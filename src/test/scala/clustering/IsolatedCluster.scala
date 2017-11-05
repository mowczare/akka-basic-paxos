package clustering

import actors.BasicPaxosProcessActor
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory

/**
  * Created by neo on 05.11.17.
  */

abstract class IsolatedCluster(_system: ActorSystem) extends TestKit(_system) with DefaultTimeout with ImplicitSender {

  def this() = this(IsolatedCluster.getSystem)

}

object IsolatedCluster {

  def getSystem = {
    val system = getSystemWithoutSharding
    setupSharding(system)
    system
  }

  def getSystemWithoutSharding = {
    val pathPrefix = "test"

    val config = ConfigFactory.parseString(s"""
      akka {
        loglevel = "INFO"

        cluster.metrics.enabled=off
        actor.provider = "akka.cluster.ClusterActorRefProvider"

        remote {
          log-remote-lifecycle-events = off
          netty.tcp {
            hostname = "127.0.0.1"
            port = 0
          }
        }
      }
    """)

    ActorSystem("basic-paxos-test", config)
  }

  def setupSharding(implicit system: ActorSystem) = {
    // Join cluster
    val cluster = Cluster(system)
    cluster.join(cluster.selfAddress)
    // Start sharding
    BasicPaxosProcessActor.clusterSharding()
  }
}