package controllers

import javax.inject._

import org.apache.pekko
import pekko.http.scaladsl.model.ContentType
import pekko.actor.typed.ActorRef
import pekko.stream.scaladsl.Flow
import pekko.stream.scaladsl.Sink
import pekko.stream.scaladsl.Source

import play.api._
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import services.SseSource
import services.SubReceptionist

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
    val controllerComponents: ControllerComponents,
    subReceptionist: ActorRef[SubReceptionist.Command]
)(implicit ec: ExecutionContext)
    extends BaseController {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def random(int: Int) = Action { implicit request: Request[AnyContent] =>
    val randomNumber = scala.util.Random.nextInt(1000)

    if (int == 1) {
      subReceptionist ! SubReceptionist.BroadCast(
        randomNumber.toString(),
        "one"
      )
    } else {
      subReceptionist ! SubReceptionist.BroadCast(
        randomNumber.toString(),
        "two"
      )
    }

    Ok(views.html.random(randomNumber))
  }

  def sse(int: Int) = Action {
    if (int == 1) {
      val source = SseSource.apply(subReceptionist, "one")
      Ok.chunked(source.via(EventSource.flow))
        .as(ContentTypes.EVENT_STREAM)
    } else {
      val source = SseSource.apply(subReceptionist, "two")
      Ok.chunked(source.via(EventSource.flow))
        .as(ContentTypes.EVENT_STREAM)
    }
  }
}
