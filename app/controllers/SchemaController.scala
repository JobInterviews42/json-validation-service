package controllers

import com.fasterxml.jackson.core.JsonParseException
import exceptions.{AlreadyExistsException, NotFoundException}
import models.PlayJsonSupport._
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import repository.AsyncRepository
import services.JsonValidationService
import utils.JsonHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import ServiceAction._
import OperationStatus._

class SchemaController @Inject()(val controllerComponents: ControllerComponents,
                                 repository: AsyncRepository,
                                 jsonValidationService: JsonValidationService)
                                (implicit executionContext: ExecutionContext) extends BaseController with Logging {

  def uploadSchema(schemaId: String) = Action.async { request =>
    Try {
      getBody(request)
      //TODO add schema validation here
    } match {
      case Success(value) =>
        val minifiedJson = Json.stringify(Json.parse(value))
        repository.storeSchema(Schema(schemaId, minifiedJson)).map {
          case Left(1) => Created(Json.toJson(OperationResult(UploadSchema, schemaId, Successful)))
          case Left(_) =>
            val response = OperationResult(UploadSchema, schemaId, Error, Some("Error persisting schema in schema storage"))
            InternalServerError(Json.toJson(response))
          case Right(exception) => respondWithError(UploadSchema, schemaId, exception)
        }.recover {
          case ex: Exception => respondWithError(UploadSchema, schemaId, ex)
        }
      case Failure(exception) => Future(respondWithError(UploadSchema, schemaId, exception))
    }
  }

  def getSchema(schemaId: String): Action[AnyContent] = Action.async {
    repository.getSchema(schemaId).map {
      case Left(schema) => Ok(Json.parse(schema.raw))
      case Right(exception) => respondWithError(GetSchema, schemaId, exception)
    }.recover {
      case ex: Exception => respondWithError(GetSchema, schemaId, ex)
    }
  }

  def validate(schemaId: String) = Action.async { request =>
    repository.getSchema(schemaId).map {
      case Left(schema) =>
       val preparedJson = Json.stringify(JsonHelper.removeNulls(Json.parse(getBody(request)).as[JsObject]))
        jsonValidationService.validateJson(preparedJson, schema.raw) match {
          case Left(_) => Ok(Json.toJson(OperationResult(ValidateDocument, schemaId, Successful)))
          case Right(validationErrors) =>
            BadRequest(Json.toJson(OperationResult(ValidateDocument, schemaId, Error, Option(validationErrors.mkString(", ")))))
        }
      case Right(exception) => respondWithError(ValidateDocument, schemaId, exception)
    }.recover {
      case ex: Exception => respondWithError(ValidateDocument, schemaId, ex)
    }
  }

  private def respondWithError(action: ServiceAction, schemaId: String, exception: Throwable) = {
    val response = OperationResult(action, schemaId, Error, Option(exception.getMessage))
    logger.error(s"Error occurred while performing action '${action.code}' for schemaId $schemaId", exception)
    getResponseByException(exception, response)
  }

  //such body extraction is quite questionable - but it was forced by curl -d arg (which sets 'application/x-www-form-urlencoded' content type)
  private def getBody(request: Request[AnyContent]) = request.body.asFormUrlEncoded.getOrElse(Map.empty).keys.mkString

  private def getResponseByException(exception: Throwable, response: OperationResult): Result = {
    val jsonResponse = Json.toJson(response)
    exception match {
      case NotFoundException(_) => NotFound(jsonResponse)
      case AlreadyExistsException(_) => Conflict(jsonResponse)
      case _: JsonParseException => BadRequest(jsonResponse)
      case _ => InternalServerError(jsonResponse)
    }
  }
}