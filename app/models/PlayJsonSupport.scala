package models

import play.api.libs.json.{Json, Writes}

object PlayJsonSupport {
  implicit val operationResultWrites = Json.writes[OperationResult]
  implicit val schemaWrites = Json.writes[Schema]

  implicit object OperationStatusWrites extends Writes[OperationStatus] {
    def writes(status: OperationStatus) = Json.toJson(status.code)
  }

  implicit object ServiceActionWrites extends Writes[ServiceAction] {
    def writes(action: ServiceAction) = Json.toJson(action.code)
  }
}
