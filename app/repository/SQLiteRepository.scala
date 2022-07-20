package repository
import models.Schema
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SQLiteRepository @Inject()(database: Database)(implicit executionContext: ExecutionContext) extends AsyncRepository {
  override def storeSchema(schema: Schema): Future[Either[String, Exception]] = ???

  override def getSchema(schemaId: String): Future[Either[Schema, Exception]] = ???
}
