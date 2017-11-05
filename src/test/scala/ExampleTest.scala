import actors.BasicPaxosProcessActor
import actors.BasicPaxosProcessActor.Create
import akka.cluster.sharding.ClusterSharding
import akka.testkit.TestProbe
import clustering.IsolatedCluster
import conf.Config
import org.scalatest.{Matchers, WordSpecLike}

class ExampleTest extends WordSpecLike with Matchers {
  "Basic Paxos actorsystem" when {
    val nodes = Config.nodesIds
    "testing hard" should {
      "work" in {
        new IsolatedCluster {
          val client = TestProbe()
          val processesRegion = ClusterSharding(system).shardRegion(BasicPaxosProcessActor.typeName)
          nodes.foreach { nodeId =>
            processesRegion ! Create(nodeId)
          }
//TODO fix
//          expectMsg(SampleInfo("actor1", SampleActorState()))
        }
      }
    }
  }
}

case object PingCommand
