package services

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import scala.jdk.CollectionConverters._

class DefaultJsonValidationService extends JsonValidationService {
  override def validateJson(json: String, rawJsonSchema: String): Either[Unit, List[String]] = {
    val schemaNode = JsonLoader.fromString(rawJsonSchema)
    val schemaFactory = JsonSchemaFactory.byDefault()
    val schema = schemaFactory.getJsonSchema(schemaNode)

    val report = schema.validate(JsonLoader.fromString(json))
    if (report.isSuccess) {
      Left()
    } else {
      Right(report.asScala.toList.map(_.getMessage))
    }
  }
}
