package models

import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

object PlayJsonSupportInTests {
  implicit val schemaReads = Json.reads[Schema]
  implicit val operationResultReads = Json.reads[OperationResult]

  implicit object OperationStatusReads extends Reads[OperationStatus] {
    override def reads(json: JsValue): JsResult[OperationStatus] = json.as[JsString].value match {
      case "success" => JsSuccess(OperationStatus.Successful)
      case "error" => JsSuccess(OperationStatus.Error)
      case unsupported => JsError(s"Unsupported value for operation status: $unsupported")
    }
  }

  implicit object ServiceActionReads extends Reads[ServiceAction] {
    override def reads(json: JsValue): JsResult[ServiceAction] = json.as[JsString].value match {
      case "uploadSchema" => JsSuccess(ServiceAction.UploadSchema)
      case "getSchema" => JsSuccess(ServiceAction.GetSchema)
      case "validateDocument" => JsSuccess(ServiceAction.ValidateDocument)
      case unsupported => JsError(s"Unsupported value for service action: $unsupported")
    }
  }
}
