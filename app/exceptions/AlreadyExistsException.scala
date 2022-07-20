package exceptions

case class AlreadyExistsException(message: String) extends RuntimeException(message)
