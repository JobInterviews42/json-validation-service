package models

sealed abstract class OperationStatus(val code: String)

object OperationStatus {
  case object Successful extends OperationStatus("success")
  case object Error extends OperationStatus("error")
}
