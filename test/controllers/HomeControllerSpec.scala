package controllers

import org.mockito.Mockito.when
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsArray, JsValue}
import play.api.routing.Router
import play.api.test.Helpers._
import play.api.test._

import javax.inject.Provider

class HomeControllerSpec extends FlatSpec with Matchers {

    "HomeController" should "render the index page from a new instance of controller" in {
      val routes = Seq(("GET", "/test1", "invocation1"), ("POST", "/test2", "invocation2"))
      val routerMock = mock[Router]
      when(routerMock.documentation).thenReturn(routes)
      val routerProviderMock = mock[Provider[Router]]
      //mockito deep stubs didn't work out here because of T taken by Provider
      when(routerProviderMock.get()).thenReturn(routerMock)

      val controller = new HomeController(stubControllerComponents(), routerProviderMock)
      val response = controller.index().apply(FakeRequest(GET, "/"))

      status(response) shouldBe OK
      contentType(response) shouldBe Some("application/json")
      val jsonResponse = contentAsJson(response)
      val actualRoutes = (jsonResponse \ "routes").get.as[JsArray].value.map {
        case item: JsValue => (item("method").as[String], item("path").as[String])
      }.toList

      actualRoutes should contain theSameElementsAs routes.map{case (method, path, _) => (method, path)}
    }
}
