package services

import org.apache.pekko
import pekko.actor.typed.ActorRef
import pekko.actor.typed.Behavior
import pekko.actor.typed.receptionist.Receptionist
import pekko.actor.typed.receptionist.ServiceKey
import pekko.actor.typed.scaladsl.Behaviors

import pekko.stream.scaladsl.Source
import pekko.stream.typed.scaladsl.ActorSource

import scala.concurrent.ExecutionContext

trait CborSerializable

object SseSource {

  def apply(
      receptionist: ActorRef[SubReceptionist.Command],
      key: String
  )(implicit
      ec: ExecutionContext
  ): Source[String, ActorRef[String]] = {
    ActorSource
      .actorRef[String](
        completionMatcher =
          // never complete the stream because of a message
          PartialFunction.empty,
        failureMatcher =
          // never fail the stream because of a message
          PartialFunction.empty,
        bufferSize = 1,
        overflowStrategy = pekko.stream.OverflowStrategy.dropHead
      )
      .mapMaterializedValue { ref =>
        println(s"created SseSource: ${ref}")
        receptionist ! SubReceptionist.Register(ref, key)
        ref
      }
      .watchTermination() { case (ref, future) =>
        future.onComplete { r =>
          println(s"terminated ${ref}")
          // todo
          // maybe, watch SseActor and terminate it when SseSource is terminated
        }
        ref
      }
  }
}

object SseActor {
  case class WrappedString(str: String) extends CborSerializable

  def apply(sseSourceRef: ActorRef[String]): Behavior[WrappedString] = {
    Behaviors.setup { context =>
      Behaviors.receiveMessage { case WrappedString(str) =>
        println(s"tell ${str} to sseSourceRef ${sseSourceRef} ")
        sseSourceRef ! str
        Behaviors.same
      }
    }
  }
}

object SubReceptionist {
  import SseActor.WrappedString

  sealed trait Command
  case class Register(sseSourceRef: ActorRef[String], key: String)
      extends Command
  case class BroadCast(msg: String, key: String) extends Command
  case object IgnoringReply extends Command

  def apply: Behavior[Command] = {

    Behaviors.receive {
      case (context, Register(sseSourceRef, key)) => {
        val serviceKey = ServiceKey[WrappedString](key)

        // todo
        val sseActor: ActorRef[WrappedString] =
          context.spawnAnonymous(SseActor.apply(sseSourceRef))

        context.system.receptionist ! Receptionist.Register(
          serviceKey,
          sseActor
        )
        Behaviors.same
      }

      case (context, BroadCast(msg, key)) => {
        val serviceKey = ServiceKey[SseActor.WrappedString](key)

        val broadcasting =
          context.messageAdapter[Receptionist.Listing] { listing =>
            listing
              .serviceInstances(serviceKey)
              .foreach { ref =>
                println(s"maybe tell ${msg} to ${ref}")
                ref ! WrappedString(msg)
              }
            IgnoringReply
          }
        context.system.receptionist.tell(
          Receptionist.Find(serviceKey, broadcasting)
        )
        Behaviors.same
      }

      case (_, IgnoringReply) => Behaviors.same
    }
  }
}
