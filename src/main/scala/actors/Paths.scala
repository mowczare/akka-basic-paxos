package actors

import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterSharding

/**
  * Created by neo on 05.11.17.
  */

object Paths {
  def nodesPath(implicit system: ActorSystem) = ClusterSharding(system).shardRegion(BasicPaxosProcessActor.typeName)
}
