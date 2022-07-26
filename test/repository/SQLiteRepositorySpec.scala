package repository

import exceptions.AlreadyExistsException
import models.Schema
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import play.api.db.Databases
import play.api.db.evolutions.Evolutions

import scala.concurrent.ExecutionContext.Implicits.global

class SQLiteRepositorySpec extends FlatSpec with Matchers with ScalaFutures with OptionValues {

  private val sqliteDriver = "org.sqlite.JDBC"
  private val dbUrl = "jdbc:sqlite:repository-test.db"
  private val defaultTestSchema = Schema("test-id", "raw")

  "SQLiteRepository" should "insert schema if such id doesn't exist yet" in {
    Databases.withDatabase(
      driver = sqliteDriver,
      url = dbUrl
    ) { database =>
      Evolutions.withEvolutions(database) {
        val repository = new SQLiteRepository(database)

        whenReady(repository.storeSchema(defaultTestSchema)) { result =>
          result shouldBe Left(1)
        }
      }
    }
  }

  it should "not allow to insert two schemas with the same id" in {
    Databases.withDatabase(
      driver = sqliteDriver,
      url = dbUrl
    ) { database =>
      Evolutions.withEvolutions(database) {
        val repository = new SQLiteRepository(database)

        whenReady(repository.storeSchema(defaultTestSchema)) { first =>
          first shouldBe Left(1)
          whenReady(repository.storeSchema(defaultTestSchema)) { second =>
            second.toOption.value shouldBe a[AlreadyExistsException]
          }
        }
      }
    }
  }
}
