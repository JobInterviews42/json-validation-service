package controllers

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.Router

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, routerProvider: Provider[Router]) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    val routes = Map(
      "routes" -> routerProvider.get().documentation.map {
        case (method, path, _) => Map(
          "method" -> method,
          "path" -> path
        )
      }
    )
    Ok(Json.toJson(routes))
  }
}
