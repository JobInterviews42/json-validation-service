package controllers

import akka.util.ByteString
import exceptions.{AlreadyExistsException, NotFoundException}
import models.PlayJsonSupportInTests._
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.{Inside, Matchers, Outcome, fixture}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import repository.AsyncRepository
import services.JsonValidationService
import utils.JsonHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchemaControllerSpec extends fixture.FlatSpec with Matchers with Inside {

  private val notFoundException = NotFoundException("Schema is not found")
  private val runtimeException = new RuntimeException("Something went wrong")
  private val validSchema = Schema("test-id", JsonHelper.validJsonSchemaV1)

  case class FixtureParam(repositoryMock: AsyncRepository, validationServiceMock: JsonValidationService)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val fixture =  FixtureParam(mock[AsyncRepository], mock[JsonValidationService])
    withFixture(test.toNoArgTest(fixture))
  }

  "SchemaController" should "return a schema by a given id" in { fixture =>
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Left(validSchema)))

    val response = createController(fixture).getSchema("test-id")(FakeRequest())
    contentType(response) shouldBe Some("application/json")
    val responseContent = contentAsJson(response)

    Json.stringify(responseContent) shouldBe Json.stringify(Json.parse(validSchema.raw))
    status(response) shouldBe OK
  }

  it should "return error status if schema by a given id doesn't exist" in { fixture =>
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(notFoundException)))

    val response = createController(fixture).getSchema("test-id")(FakeRequest())

    status(response) shouldBe NOT_FOUND
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(notFoundException.getMessage))
  }

  it should "return internal server error if some unexpected exception happened" in { fixture =>
   when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(runtimeException)))

    val response = createController(fixture).getSchema("test-id")(FakeRequest())

    status(response) shouldBe INTERNAL_SERVER_ERROR
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(runtimeException.getMessage))
  }

  it should "return internal server error if async call to repository crashed" in { fixture =>
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.failed(runtimeException))

    val response = createController(fixture).getSchema("test-id")(FakeRequest())

    status(response) shouldBe INTERNAL_SERVER_ERROR
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(runtimeException.getMessage))
  }

  it should "upload schema if there is no schema with such id exists" in { fixture =>
    when(fixture.repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.successful(Left(1)))

    val response = createController(fixture).uploadSchema("test-id")(FakeRequest().withFormUrlEncodedBody((JsonHelper.validJsonSchemaV1, "")))

    status(response) shouldBe CREATED
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Success, None)
  }

  it should "not upload schema if there is a schema with such id exists" in { fixture =>
    val exception = AlreadyExistsException("schema already exists")
    when(fixture.repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.successful(Right(exception)))

    val response = createController(fixture).uploadSchema("test-id")(FakeRequest().withFormUrlEncodedBody((JsonHelper.validJsonSchemaV1, "")))

    status(response) shouldBe CONFLICT
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Error, Option(exception.message))
  }

  it should "return internal server error if a call to repository crashed during schema upload" in { fixture =>
    when(fixture.repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.failed(runtimeException))
    val response = createController(fixture).uploadSchema("test-id")(FakeRequest().withFormUrlEncodedBody((JsonHelper.validJsonSchemaV1, "")))

    status(response) shouldBe INTERNAL_SERVER_ERROR
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Error, Option(runtimeException.getMessage))
  }

  it should "validate correct json successfully" in { fixture =>
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Left(validSchema)))
    when(fixture.validationServiceMock.validateJson(anyString(), anyString())).thenReturn(Left(()))

    val response = createController(fixture).validate(validSchema.schemaId)(FakeRequest().withFormUrlEncodedBody((JsonHelper.validJsonV1, "")))
    status(response) shouldBe OK
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.ValidateDocument, validSchema.schemaId, OperationStatus.Success, None)
  }

  it should "not validate json if specified schema doesn't exist" in { fixture =>
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(notFoundException)))

    val response = createController(fixture).validate("nonExisting")(FakeRequest().withRawBody(ByteString(JsonHelper.validJsonV1)))
    status(response) shouldBe NOT_FOUND
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.ValidateDocument, "nonExisting", OperationStatus.Error, Option(notFoundException.getMessage))
  }

  it should "return error with error messages if json is not valid against specified schema" in { fixture =>
    val validationErrorMsg = "some validation error message"
    when(fixture.repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Left(validSchema)))
    when(fixture.validationServiceMock.validateJson(anyString(), anyString())).thenReturn(Right(List(validationErrorMsg)))

    val response = createController(fixture).validate("schemaId")(FakeRequest().withFormUrlEncodedBody((JsonHelper.validJsonV1, "")))
    status(response) shouldBe BAD_REQUEST
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.ValidateDocument, "schemaId", OperationStatus.Error, Some(validationErrorMsg))
  }

  private def createController(fixture: FixtureParam): SchemaController = new SchemaController(Helpers.stubControllerComponents(), fixture.repositoryMock, fixture.validationServiceMock)

  private def assertResult(result: OperationResult, expectedAction: ServiceAction,
                           expectedId: String, expectedStatus: OperationStatus, expectedMessage: Option[String]) = {
    inside(result) {
      case OperationResult(action, id, status, message) =>
        action shouldBe expectedAction
        id shouldBe expectedId
        status shouldBe expectedStatus
        message shouldBe expectedMessage
    }
  }
}
