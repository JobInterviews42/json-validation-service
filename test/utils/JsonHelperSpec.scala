package utils

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

class JsonHelperSpec extends FlatSpec with Matchers {

  "JsonHelper" should "remove nulls recursively" in {
    val expectedJson = Json.stringify(Json.parse(JsonSamples.validJsonV2))
    val refinedJson = JsonHelper.removeNulls(Json.parse(JsonSamples.validJsonWithNullsV2).as[JsObject])

    Json.stringify(refinedJson) shouldBe expectedJson
  }

}
