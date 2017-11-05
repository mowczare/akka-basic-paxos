import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}

class ExampleTest extends TestKit(ActorSystem("test")) with WordSpecLike with Matchers {
  "basic actor system" when {
    val someActor = TestProbe()

    "sending PingCommand" should {
      someActor.ref ! PingCommand

      "receive PingCommand" in {
        someActor.expectMsg(PingCommand)
      }
    }
  }
}

case object PingCommand
