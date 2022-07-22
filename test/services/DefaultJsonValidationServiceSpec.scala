package services

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import utils.JsonSamples._

class DefaultJsonValidationServiceSpec extends FlatSpec with Matchers with OptionValues with TableDrivenPropertyChecks {

  private val dataset = Table(
    ("testCase", "jsonDocument", "jsonSchema", "isValid"),
    ("return success if json document is valid against specified schema", validJsonV1, validJsonSchemaV1, true),
    ("return error messages if json document is invalid against specified schema", invalidJsonV1, validJsonSchemaV1, false)
  )

  forAll(dataset) { (testCase, jsonDocument, jsonSchema, isValid) =>
    it should testCase in {
      val service = new DefaultJsonValidationService()
      val result = service.validateJson(jsonDocument, jsonSchema).toOption

      if(isValid) {
        result shouldBe empty
      } else {
        result.value should not be empty
      }
    }
  }
}
