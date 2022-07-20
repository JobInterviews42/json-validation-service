package controllers

import exceptions.NotFoundException
import models.{OperationResult, OperationStatus, Schema, ServiceAction}
import org.mockito.ArgumentMatchers.anyString
import org.scalatest.{FlatSpec, Inside, Matchers}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.{FakeRequest, Helpers}
import repository.AsyncRepository
import play.api.test.Helpers._
import org.mockito.Mockito._
import play.mvc.Http
import models.PlayJsonSupportInTests._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchemaControllerSpec extends FlatSpec with Matchers with Inside {

  "SchemaController" should "return a schema by a given id" in {
    val testSchema = Schema("test-id", "raw schema definition")
    val repositoryMock = mock[AsyncRepository]
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Left(testSchema)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())
    val responseContent = contentAsJson(response).as[Schema]

    responseContent shouldBe testSchema
    status(response) shouldBe Http.Status.OK
  }

  it should "return error status if schema by a given id doesn't exist" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = NotFoundException("Schema is not found")
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(exception)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())

    status(response) shouldBe Http.Status.NOT_FOUND
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(exception.getMessage))
  }

  it should "return internal server error if some unexpected exception happened" in {
    val repositoryMock = mock[AsyncRepository]
    val exception = new RuntimeException("Schema is not found")
    when(repositoryMock.getSchema(anyString())).thenReturn(Future.successful(Right(exception)))

    val controller = new SchemaController(Helpers.stubControllerComponents(), repositoryMock)

    val response = controller.getSchema("test-id")(FakeRequest())

    status(response) shouldBe Http.Status.INTERNAL_SERVER_ERROR
    assertResult(contentAsJson(response).as[OperationResult], ServiceAction.GetSchema,
      "test-id", OperationStatus.Error, Option(exception.getMessage))
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
