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
  val client = TestProbe()
  val processesRegion = ClusterSharding(system).shardRegion(BasicPaxosProcessActor.typeName)
  implicit val standardTimeout = 10 seconds

  "Basic Paxos actorsystem" when {

    "all nodes are created" should {

      nodes.foreach { nodeId =>
        processesRegion ! Create(nodeId)
      }

      "correctly read value from all nodes after write" in {
        val newValue = "newValue1"
        processesRegion ! WriteValue(nodes.head, client.ref, newValue)
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          processesRegion ! ReadValue(nodeId, client.ref)
          eventually {
            client.expectMsg(ReadResponse(Some(newValue)))
          }
        }
      }

      "correctly read value from all nodes after update" in {
        val newValue = "newValue2"
        processesRegion ! WriteValue(nodes.tail.head, client.ref, newValue)
        client.expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          processesRegion ! ReadValue(nodeId, client.ref)
          eventually {
            client.expectMsg(ReadResponse(Some(newValue)))
          }
        }
      }

      "Read latter value when two sequential writes are served" in { //DUELING PROPOSERS
        val badValue = "someGarbage"
        val newValue = "newValue3"
        processesRegion ! WriteValue(nodes.head, client.ref, badValue)
        processesRegion ! WriteValue(nodes.tail.head, client.ref, newValue)
        expectMsg(WriteSucceeded(newValue))

        nodes.foreach { nodeId =>
          processesRegion ! ReadValue(nodeId, client.ref)
          eventually {
            client.expectMsg(standardTimeout, ReadResponse(Some(newValue)))
          }
        }
      }


      "Get no Write response when half of the nodes are dead" in {
        val oldValue = "newValue3"
        val newValue = "newValue4"
        nodesIds.slice(nodesIds.size/2, nodesIds.size).foreach(nodeId => processesRegion ! Kill(nodeId))
        processesRegion ! WriteValue(nodes.head, client.ref, newValue)
        client.expectNoMessage(standardTimeout)

        nodes.foreach { nodeId =>
          processesRegion ! ReadValue(nodeId, client.ref)
          client.expectMsg(ReadResponse(Some(oldValue)))
        }
      }

      //TODO ACCEPTOR FAIL, PROPOSER FAIL, FIX TESTS





    }
  }
}