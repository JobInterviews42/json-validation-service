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

  val validJsonWithNullsV2 =
    """
      |{
      |	"random": null,
      |	"random float": "5.466",
      |	"bool": "true",
      |	"date": "1989-11-26",
      |	"regEx": "hellooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo to you",
      |	"enum": "generator",
      |	"firstname": "Joceline",
      |	"lastname": "McLaughlin",
      |	"city": "Murmansk",
      |	"country": "Barbados",
      |	"countryCode": "NP",
      |	"email uses current data": "Joceline.McLaughlin@gmail.com",
      |	"email from expression": "Joceline.McLaughlin@yopmail.com",
      |	"array": [
      |		"Heddie",
      |		"Wynne",
      |		"Gale",
      |		"Marleah",
      |		"Aili"
      |	],
      |	"array of objects": [
      |		{
      |			"index": "0",
      |			"index start at 5": "5"
      |		},
      |		{
      |			"index": "1",
      |			"index start at 5": "6"
      |		},
      |		{
      |			"index": "2",
      |			"index start at 5": {"name": null }
      |		}
      |	],
      |	"Frances": {
      |		"age": null
      |	}
      |}
      |""".stripMargin

  val validJsonV2 =
    """
      |{
      |	"random float": "5.466",
      |	"bool": "true",
      |	"date": "1989-11-26",
      |	"regEx": "hellooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo to you",
      |	"enum": "generator",
      |	"firstname": "Joceline",
      |	"lastname": "McLaughlin",
      |	"city": "Murmansk",
      |	"country": "Barbados",
      |	"countryCode": "NP",
      |	"email uses current data": "Joceline.McLaughlin@gmail.com",
      |	"email from expression": "Joceline.McLaughlin@yopmail.com",
      |	"array": [
      |		"Heddie",
      |		"Wynne",
      |		"Gale",
      |		"Marleah",
      |		"Aili"
      |	],
      |	"array of objects": [
      |		{
      |			"index": "0",
      |			"index start at 5": "5"
      |		},
      |		{
      |			"index": "1",
      |			"index start at 5": "6"
      |		},
      |		{
      |			"index": "2",
      |			"index start at 5": {}
      |		}
      |	],
      |	"Frances": {
      |	}
      |}
      |""".stripMargin

}
