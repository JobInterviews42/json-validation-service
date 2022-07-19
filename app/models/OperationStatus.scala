package models

sealed abstract class OperationStatus(val code: String)

object OperationStatus {
  case object Success extends OperationStatus("success")
  case object Error extends OperationStatus("error")
}
