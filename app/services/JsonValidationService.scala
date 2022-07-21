package services

trait JsonValidationService {
  /**
   *
   * @param json
   * @param rawJsonSchema
   * @return either Unit if validation is successful, or a list of errors found during the validation
   */
  def validateJson(json: String, rawJsonSchema: String):  Either[Unit, List[String]]
}
