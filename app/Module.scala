import com.google.inject._
import play.api.libs.concurrent.PekkoGuiceSupport
import services.SubReceptionist

class Module extends AbstractModule with PekkoGuiceSupport {
  override def configure() = {
    bindTypedActor(SubReceptionist.apply, "subReceptionist")
  }
}

// here
// import services.ReceptionistManager
// class Module extends AbstractModule with PekkoGuiceSupport {
//   override def configure() = {
//     bindTypedActor(ReceptionistManager.apply(), "receptionistManager")
//   }
// }
