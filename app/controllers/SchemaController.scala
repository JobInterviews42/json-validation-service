package controllers

import com.fasterxml.jackson.core.JsonParseException
import exceptions.{AlreadyExistsException, NotFoundException}
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result}
import repository.AsyncRepository
import models.PlayJsonSupport._
import play.api.Logging
import play.api.libs.json.{JsArray, JsNull, JsObject, JsValue, Json}
import services.JsonValidationService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
          case Left(1) => Created(Json.toJson(OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Success)))
          case Left(_) =>
            val response = OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Some("Error persisting schema in schema storage"))
            InternalServerError(Json.toJson(response))
          case Right(exception) =>
            val response = OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Option(exception.getMessage))
            getResponseByException(exception, response)
        }.recover {
          case ex: Exception =>
            val response = OperationResult(ServiceAction.UploadSchema, schemaId, OperationStatus.Error, Option(ex.getMessage))
            logger.error(s"A call to repository has crashed while uploading schema with id $schemaId", ex)
            getResponseByException(ex, response)
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

  def validate(schemaId: String): Action[AnyContent] = Action.async { request =>
    repository.getSchema(schemaId).map {
      case Left(schema) =>
        val rawJsonDocument = getBody(request)
        val preparedJson = Json.stringify(removeNulls(Json.parse(rawJsonDocument).as[JsObject]))
        jsonValidationService.validateJson(preparedJson, schema.raw) match {
          case Left(_) => Ok(Json.toJson(OperationResult(ServiceAction.ValidateDocument, schemaId, OperationStatus.Success)))
          case Right(validationErrors) =>
            BadRequest(Json.toJson(OperationResult(ServiceAction.ValidateDocument, schemaId, OperationStatus.Error, Option(validationErrors.mkString(", ")))))
        }

      case Right(exception) =>
        val response = OperationResult(ServiceAction.ValidateDocument, schemaId, OperationStatus.Error, Option(exception.getMessage))
        logger.error(s"Error while validating json document against schema with id $schemaId", exception)
        getResponseByException(exception, response)
    }.recover {
      case ex: Exception =>
        val response = OperationResult(ServiceAction.ValidateDocument, schemaId, OperationStatus.Error, Option(ex.getMessage))
        logger.error(s"A call to repository has crashed while validating json document using schemaId $schemaId", ex)
        getResponseByException(ex, response)
    }
  }

  private def getResponseByException(exception: Exception, response: OperationResult): Result = {
    val jsonResponse = Json.toJson(response)
    exception match {
      case NotFoundException(_) => NotFound(jsonResponse)
      case AlreadyExistsException(_) => Conflict(jsonResponse)
      case _: JsonParseException => BadRequest(jsonResponse)
      case _ => InternalServerError(jsonResponse)
    }
  }

  private def getBody(request: Request[AnyContent]): String = request.body.asRaw.get.asBytes().get.utf8String

  private def removeNulls(jsObject: JsObject): JsValue = {
    JsObject(jsObject.fields.collect {
      case (fieldName, fieldValue: JsObject) =>
        (fieldName, removeNulls(fieldValue))

      case (arrayName, arrayValue: JsArray) =>
        val filteredItems = arrayValue.value.collect {
          case objItem: JsObject => removeNulls(objItem)
          case otherItem if otherItem != JsNull  => otherItem
        }
        (arrayName, JsArray(filteredItems))

      case other if (other._2 != JsNull) =>
        other
    })
  }

}