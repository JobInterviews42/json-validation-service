package controllers

import exceptions.{AlreadyExistsException, NotFoundException}
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}
import repository.AsyncRepository
import models.PlayJsonSupport._
import play.api.Logging
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class SchemaController @Inject()(val controllerComponents: ControllerComponents, repository: AsyncRepository)
                                (implicit executionContext: ExecutionContext) extends BaseController with Logging {

  def uploadSchema(schemaId: String) = Action.async(parse.raw) { request =>
    Try {
      request.body.asBytes().map(_.utf8String)
      //TODO add schema validation here
    } match {
      case Success(Some(value)) =>
        val minifiedJson = Json.stringify(Json.parse(value))
        repository.storeSchema(Schema(schemaId, minifiedJson)).map {
          case Left(1) => Created(Json.toJson(OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Success)))
          case Left(_) =>
            val response = OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Some("Error persisting schema in schema storage"))
            InternalServerError(Json.toJson(response))
          case Right(exception) =>
            val response = OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Option(exception.getMessage))
            getResponseByException(exception, response)
        }
      case Failure(exception) =>
        logger.error("Error parsing json schema", exception)
        Future(BadRequest(Json.toJson(OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Option(exception.getMessage)))))
    }
  }

  def getSchema(schemaId: String): Action[AnyContent] = Action.async {
    repository.getSchema(schemaId).map {
      case Left(schema) => Ok(Json.parse(schema.raw))
      case Right(exception) =>
        val response = OperationResult(ServiceAction.GetSchema, schemaId, OperationStatus.Error, Option(exception.getMessage))
        logger.error(s"Error while reading schema with id $schemaId", exception)
        getResponseByException(exception, response)
    }.recover {
      case ex: Exception =>
        val response = OperationResult(ServiceAction.GetSchema, schemaId, OperationStatus.Error, Option(ex.getMessage))
        logger.error(s"A call to repository has crashed while reading schema with id $schemaId", ex)
        getResponseByException(ex, response)
    }
  }

  def validate(schemaId: String): Action[AnyContent] = Action.async {
    Future.successful(Ok(s"TODO: validate schemaId $schemaId"))
  }

  private def getResponseByException(exception: Exception, response: OperationResult): Result = {
    val jsonResponse = Json.toJson(response)
    exception match {
      case NotFoundException(_) => NotFound(jsonResponse)
      case AlreadyExistsException(_) => Conflict(jsonResponse)
      case _ => InternalServerError(jsonResponse)
    }
  }

}