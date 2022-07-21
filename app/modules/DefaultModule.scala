package modules

import play.api.{Configuration, Environment}
import repository.{AsyncRepository, SQLiteRepository}
import services.{DefaultJsonValidationService, JsonValidationService}

class DefaultModule extends play.api.inject.Module {
  def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[AsyncRepository].to(classOf[SQLiteRepository]),
      bind[JsonValidationService].to(classOf[DefaultJsonValidationService])
    )
  }
}
