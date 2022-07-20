package repository

import exceptions.{AlreadyExistsException, NotFoundException}
import models.Schema
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SQLiteRepository @Inject()(database: Database)(implicit executionContext: ExecutionContext) extends AsyncRepository {

  private val getSchemaByIdSql = "SELECT rawSchema FROM JsonSchema WHERE id = ?"
  private val insertSchemaSql = "INSERT INTO JsonSchema (id, rawSchema) VALUES(?, ?)"

  override def storeSchema(schema: Schema): Future[Either[Int, Exception]] = getSchema(schema.schemaId).map {
    case Left(_) => Right(AlreadyExistsException(s"Schema with id ${schema.schemaId} already exists"))
    case Right(NotFoundException(_)) => Left(insertSchema(schema))
    case Right(other) => Right(other) //other exceptions are propagated as is
  }

  private def insertSchema(schema: Schema): Int = {
    database.withConnection { connection =>
      val statement = connection.prepareStatement(insertSchemaSql)
      statement.setString(1, schema.schemaId)
      statement.setString(2, schema.raw)
      val result = statement.executeUpdate()
      statement.close()
      result
    }
  }

  override def getSchema(schemaId: String): Future[Either[Schema, Exception]] = Future {
    database.withConnection { connection =>
      val statement = connection.prepareStatement(getSchemaByIdSql)
      statement.setString(1, schemaId)
      val rs = statement.executeQuery()
      val result = if (rs.next()) {
        Left(Schema(schemaId, rs.getString(1)))
      } else {
        Right(NotFoundException(s"Schema with id '$schemaId' not found"))
      }
      rs.close()
      statement.close()

      result
    }
  }
}
