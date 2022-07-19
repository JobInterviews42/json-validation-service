package models

case class OperationResult(action: ServiceAction, id: String, status: OperationStatus, message: Option[String] = None)
