package utils

import play.api.libs.json.{JsArray, JsNull, JsObject, JsValue}

object JsonHelper {

  def removeNulls(jsObject: JsObject): JsValue = {
    JsObject(jsObject.fields.collect {
      case (fieldName, fieldValue: JsObject) =>
        (fieldName, removeNulls(fieldValue))

      case (arrayName, arrayValue: JsArray) =>
        val filteredItems = arrayValue.value.collect {
          case objItem: JsObject => removeNulls(objItem)
          case otherItem if otherItem != JsNull  => otherItem
        }
        (arrayName, JsArray(filteredItems))

      case other if (other._2 != JsNull) =>
        other
    })
  }
}
