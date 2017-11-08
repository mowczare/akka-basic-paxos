package conf

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

object Config {
  val config = ConfigFactory.load()
  val systemName = config.getString("basic-paxos.system.name")
  val nodesIds = config.getStringList("basic-paxos.nodes-ids").asScala
  val numberOfNodes = nodesIds.size
  val apiHost = config.getString("basic-paxos.apiHost")
  val apiPort = config.getInt("basic-paxos.apiPort")
}
