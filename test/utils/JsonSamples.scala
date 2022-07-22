package utils

object JsonSamples {

  val validJsonSchemaV1 =
    """{
      |  "$schema": "http://json-schema.org/draft-04/schema#",
      |  "type": "object",
      |  "properties": {
      |    "source": {
      |      "type": "string"
      |    },
      |    "destination": {
      |      "type": "string"
      |    },
      |    "timeout": {
      |      "type": "integer",
      |      "minimum": 0,
      |      "maximum": 32767
      |    },
      |    "chunks": {
      |      "type": "object",
      |      "properties": {
      |        "size": {
      |          "type": "integer"
      |        },
      |        "number": {
      |          "type": "integer"
      |        }
      |      },
      |      "required": ["size"]
      |    }
      |  },
      |  "required": ["source", "destination"]
      |}""".stripMargin

  val validJsonWithNullsV1 =
    """
      |{
      |  "source": "/home/alice/image.iso",
      |  "destination": "/mnt/storage",
      |  "timeout": null,
      |  "chunks": {
      |    "size": 1024,
      |    "number": null
      |  }
      |}""".stripMargin

  val validJsonV1 =
    """
      |{
      |  "source": "/home/alice/image.iso",
      |  "destination": "/mnt/storage",
      |  "timeout": 3,
      |  "chunks": {
      |    "size": 1024,
      |    "number": 2
      |  }
      |}""".stripMargin

  val invalidJsonV1 =
    """
      |{
      |  "source": "/home/alice/image.iso",
      |  "destination": "/mnt/storage",
      |  "timeout": 3,
      |  "chunks": {
      |    "size": 1024,
      |    "number": "2"
      |  }
      |}""".stripMargin

}
