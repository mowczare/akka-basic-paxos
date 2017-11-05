package conf

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

object Config {
  val config = ConfigFactory.load()
  val systemName = config.getString("basic-paxos.system.name")
  val nodesIds = config.getStringList("basic-paxos.nodes-ids").asScala
  val numberOfNodes = nodesIds.size
}
