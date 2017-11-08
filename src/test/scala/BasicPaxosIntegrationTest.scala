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
        client.send(processesRegion, WriteValue(nodes.head, newValue))
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          client.expectMsg(ReadResponse(Some(newValue)))
        }
      }

      "correctly read value from all nodes after update" in {
        val client = TestProbe()
        val newValue = "newValue2"
        client.send(processesRegion, WriteValue(nodes.tail.head, newValue))
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          client.expectMsg(ReadResponse(Some(newValue)))
        }
      }

/*      "Read latter value when two sequential writes are served" in { //DUELING PROPOSERS
        val client = TestProbe()
        val firstValue = "firstValue3"
        val secondValue = "secondValue3"
        val secondClient = TestProbe()
        client.send(processesRegion, WriteValue(nodes.head, firstValue))
        Thread.sleep(50)
        client.send(processesRegion, WriteValue(nodes.tail.head, secondValue))
        Thread.sleep(500)
        secondClient.expectMsg(WriteSucceeded(secondValue))
        nodes.foreach { nodeId =>
          client.send(processesRegion, ReadValue(nodeId))
          secondClient.expectMsg(ReadResponse(Some(secondValue)))
        }
      }*/
    }
  }
}