package models

sealed abstract class ServiceAction(val code: String)

object ServiceAction {
  case object UploadSchema extends ServiceAction(code = "uploadSchema")
  case object ValidateDocument extends ServiceAction(code = "validateDocument")
  case object GetSchema extends ServiceAction(code = "getSchema")
}
