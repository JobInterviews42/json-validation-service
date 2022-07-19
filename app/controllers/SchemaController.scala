package controllers

import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.Future

class SchemaController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def uploadSchema(schemaId: String) = Action.async {
    Future.successful(Ok(s"TODO: uploadSchema with id $schemaId"))
  }

  def getSchema(schemaId: String) = Action.async {
    Future.successful(Ok(s"TODO: getSchema id $schemaId"))
  }

  def validate(schemaId: String) = Action.async {
    Future.successful(Ok(s"TODO: validate schemaId $schemaId"))
  }

}
