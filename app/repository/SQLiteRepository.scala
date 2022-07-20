package repository
import models.Schema

import scala.concurrent.Future

class SQLiteRepository extends AsyncRepository {
  override def storeSchema(schema: Schema): Future[Either[String, Exception]] = ???

  override def getSchema(schemaId: String): Future[Either[Schema, Exception]] = ???
}
