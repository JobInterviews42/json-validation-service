package repository

import models.Schema

import scala.concurrent.Future

trait AsyncRepository {
  def storeSchema(schema: Schema): Future[Either[String, Exception]]
  def getSchema(schemaId: String): Future[Either[Schema, Exception]]
}
