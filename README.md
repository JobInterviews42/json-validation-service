# json-validation-service
A simple REST-service for validating JSON documents against JSON Schemas. 
The service is powered by Play Framework (v. 2.8.16). 
All uploaded json schemas are persisted into an embedded SQlite database

## Requirements
- Scala 2.13.8
- sbt 1.7.1

## How to run on localhost
Navigate to the root folder of the project and run `sbt run` command. The service base url is http://localhost:9000

## How to run tests
Navigate to the root folder of the project and run `sbt test` command


## Test scenario
Assuming we have a json schema

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "source": {
      "type": "string"
    },
    "destination": {
      "type": "string"
    },
    "timeout": {
      "type": "integer",
      "minimum": 0,
      "maximum": 32767
    },
    "chunks": {
      "type": "object",
      "properties": {
        "size": {
          "type": "integer"
        },
        "number": {
          "type": "integer"
        }
      },
      "required": ["size"]
    }
  },
  "required": ["source", "destination"]
}
```
stored in `schema.json` file, and we have
```json
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}
```
stored in `document.json` file. Additionally, we also need a json document which is not valid against the schema above
```json
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": "2",
  "chunks": {
    "size": 1024,
    "number": null
  }
}
```
stored in `invalid-document.json`.

### Success path
1. Upload schema by using the command `curl -i -X POST http://localhost:9000/schema/schema1 -d @schema.json -H 'Content-Type: application/json'`. The service responds with `{"action":"uploadSchema","id":"schema1","status":"success"}`
2. Validate json document against `schema1` by using `curl -i http://localhost:9000/validate/schema1 -X POST -d @document.json -H 'Content-Type: application/json'`. The service responds with `{"action":"validateDocument","id":"schema1","status":"success"}`.
3. Also if you execute `curl -i http://localhost:9000/schema/schema1` you will receive a previously uploaded schema back.

### Error paths
Assuming that the success path was accomplished previously, do the following
- Upload a schema with id which already exists `curl -i -X POST http://localhost:9000/schema/schema1 -d @schema.json -H 'Content-Type: application/json'`. Service responds with `{"action":"uploadSchema","id":"schema1","status":"error","message":"Schema with id 'schema1' already exists"}`
- Validate json document against non-existing schema `curl -i http://localhost:9000/validate/nonExisting -X POST -d @document.json -H 'Content-Type: application/json'`. The service responds with `{"action":"validateDocument","id":"nonExisting","status":"error","message":"Schema with id 'nonExisting' not found"}`
- Upload a schema while providing no content `curl -i -X POST http://localhost:9000/schema/schema2 -d '' -H 'Content-Type: application/json'`. The service responds with `{"action":"uploadSchema","id":"schema2","status":"error","message":"Request body is not recognized as json or empty. Please check the Content-Type request header field"}`
- Validate a document while providing no content `curl -i http://localhost:9000/validate/schema1 -X POST -d '' -H 'Content-Type: application/json'`. The service responds with `{"action":"validateDocument","id":"schema1","status":"error","message":"Request body is not recognized as json or empty. Please check the Content-Type request header field"}`

## Limitations
Service endspoints for POST requests expect a json data in the body, it is required that a client provides `Content-Type: application/json` request header field.