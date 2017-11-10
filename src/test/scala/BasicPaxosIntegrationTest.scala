import actors.BasicPaxosProcessActor
import actors.BasicPaxosProcessActor._
import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterSharding
import akka.testkit.{TestKit, TestProbe}
import conf.Config
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps


class BasicPaxosIntegrationTest extends TestKit(ActorSystem(Config.systemName)) with WordSpecLike with Matchers with Eventually {

  val nodes = Config.nodesIds
  BasicPaxosProcessActor.clusterSharding()
  val processesRegion = ClusterSharding(system).shardRegion(BasicPaxosProcessActor.typeName)
  implicit val standardTimeout = 10 seconds

  "Basic Paxos actorsystem" when {

    "all nodes are created" should {

      nodes.foreach { nodeId =>
        processesRegion ! Create(nodeId)
      }

      "correctly read value from all nodes after write" in {
        val client = TestProbe()
        val newValue = "newValue1"
        val node = nodes.head
        client.send(processesRegion, WriteValue(node, newValue))
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          client.expectMsg(ReadResponse(Some(newValue)))
        }
      }

      "correctly read value from all nodes after update other node" in {
        val client = TestProbe()
        val newValue = "newValue2"
        val node = nodes.tail.head
        client.send(processesRegion, WriteValue(node, newValue))
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          client.expectMsg(ReadResponse(Some(newValue)))
        }
      }

      "have all nodes updated with the same value after two sequential writes" in {
        val client = TestProbe()
        val newValue = "newValue3a"
        val newOtherValue = "newValue3b"
        client.send(processesRegion, WriteValue(nodes.tail.head, newValue))
        client.send(processesRegion, WriteValue(nodes.head, newOtherValue))
        client.expectMsgPF() {
          case WriteSucceeded(v) =>
        }

        val result = nodes.map{ nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          client.expectMsgPF() {
            case ReadResponse(valueWritten) if List(newValue, newOtherValue).contains(valueWritten.get) => valueWritten
          }
        }
        result.foreach(el => el shouldBe result.head)
      }
    }
  }
}