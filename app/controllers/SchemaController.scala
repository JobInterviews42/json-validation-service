package controllers

import exceptions.NotFoundException
import models.{OperationResult, OperationStatus, ServiceAction}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}
import repository.AsyncRepository
import models.PlayJsonSupport._
import play.api.Logging
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemaController @Inject()(val controllerComponents: ControllerComponents, repository: AsyncRepository)
                                (implicit executionContext: ExecutionContext) extends BaseController with Logging {

  def uploadSchema(schemaId: String): Action[AnyContent] = Action.async {
    Future.successful(Ok(s"TODO: uploadSchema with id $schemaId"))
  }

  def getSchema(schemaId: String): Action[AnyContent] = Action.async {
    repository.getSchema(schemaId).map {
      case Left(schema) => Ok(Json.toJson(schema))
      case Right(exception) =>
        val response = OperationResult(ServiceAction.GetSchema, schemaId, OperationStatus.Error, Option(exception.getMessage))
        logger.error(s"Error while reading schema with id $schemaId", exception)
        getErrorResponse(exception, response)
    }.recover {
      case ex: Exception =>
        val response = OperationResult(ServiceAction.GetSchema, schemaId, OperationStatus.Error, Option(ex.getMessage))
        logger.error(s"A call to repository has crashed while reading schema with id $schemaId", ex)
        getErrorResponse(ex, response)
    }
  }

  def validate(schemaId: String): Action[AnyContent] = Action.async {
    Future.successful(Ok(s"TODO: validate schemaId $schemaId"))
  }

  private def getErrorResponse(exception: Exception, response: OperationResult): Result = {
    val jsonResponse = Json.toJson(response)
    exception match {
      case NotFoundException(_) => NotFound(jsonResponse)
      case _ => InternalServerError(jsonResponse)
    }
  }

}
