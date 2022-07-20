package controllers

import akka.util.ByteString
import exceptions.{AlreadyExistsException, NotFoundException}
import models.PlayJsonSupportInTests._
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Inside, Matchers}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import repository.AsyncRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchemaControllerSpec extends FlatSpec with Matchers with Inside {

  private val testSchema =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "title": "Product",
      |    "description": "A product from the catalog",
      |    "type": "object",
      |    "properties": {
      |        "id": {
      |            "description": "The unique identifier for a product",
      |            "type": "integer"
      |        },
      |        "name": {
      |            "description": "Name of the product",
      |            "type": "string"
      |        },
      |        "price": {
      |            "type": "number",
      |            "minimum": 0,
      |            "exclusiveMinimum": true
      |        }
      |    },
      |    "required": ["id", "name", "price"]
      |}
      |""".stripMargin

  "SchemaController" should "return a schema by a given id" in {
    val schema = Schema("test-id", testSchema)
    val repositoryMock = mock[AsyncRepository]
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Left(schema)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())
    contentType(response) shouldBe Some("application/json")
    val responseContent = contentAsJson(response)

    Json.stringify(responseContent) shouldBe Json.stringify(Json.parse(schema.raw))
    status(response) shouldBe OK
  }

  it should "return error status if schema by a given id doesn't exist" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = NotFoundException("Schema is not found")
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(exception)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())

    status(response) shouldBe NOT_FOUND
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(exception.getMessage))
  }

  it should "return internal server error if some unexpected exception happened" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = new RuntimeException("Schema is not found")
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(exception)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())

    status(response) shouldBe INTERNAL_SERVER_ERROR
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(exception.getMessage))
  }

  it should "return internal server error if async call to repository crashed" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = new RuntimeException("Unknown error")
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.failed(exception))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())

    status(response) shouldBe INTERNAL_SERVER_ERROR
    contentType(response) shouldBe Some("application/json")
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(exception.getMessage))
  }

  it should "upload schema if there is no schema with such id exists" in {
    val repositoryMock = mock[AsyncRepository]
    when(repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.successful(Left(1)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)
    val response = controller.uploadSchema("test-id")(FakeRequest().withRawBody(ByteString(testSchema)))

    status(response) shouldBe CREATED
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Success, None)
  }

  it should "not upload schema if there is a schema with such id exists" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = AlreadyExistsException("schema already exists")
    when(repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.successful(Right(exception)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)
    val response = controller.uploadSchema("test-id")(FakeRequest().withRawBody(ByteString(testSchema)))

    status(response) shouldBe CONFLICT
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Error, Option(exception.message))
  }

  it should "return internal server error if a call to repository crashed during schema upload" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = new RuntimeException("unknown error")
    when(repositoryMock.storeSchema(any(classOf[Schema]))).thenReturn(Future.failed(exception))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)
    val response = controller.uploadSchema("test-id")(FakeRequest().withRawBody(ByteString(testSchema)))

    status(response) shouldBe INTERNAL_SERVER_ERROR
    val result = contentAsJson(response).as[OperationResult]
    assertResult(result, ServiceAction.UploadSchema, "test-id", OperationStatus.Error, Option(exception.getMessage))
  }

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
