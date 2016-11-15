package eagle.peopleapi.rest.IoC

import com.google.inject.PrivateModule
import com.google.inject.Singleton
import eagle.common.traits.Translator
import eagle.peopleapi.rest.datacontracts.DatatrustMatchRequest
import eagle.peopleapi.rest.datacontracts.DatatrustMatchResponse
import eagle.peopleapi.rest.http.PeopleQueryHttpService
import eagle.peopleapi.rest.http.routes.PeopleQueryServiceRoute
import eagle.peopleapi.rest.translators.DatatrustMatchRequestToPersonCriteria
import eagle.peopleapi.rest.translators.PersonToPersonDatatrustResponse
import eagle.peopleapi.service.datatrust.PeopleQuery
import eagle.peopleapi.service.datatrust.PeopleQueryService
import eagle.peopleapi.service.datatrust.models.Person
import eagle.peopleapi.service.datatrust.models.PersonCriteria
import net.codingwell.scalaguice.ScalaPrivateModule

import scala.concurrent.ExecutionContext

private[eagle] class ServicesModule extends PrivateModule with ScalaPrivateModule {

  override def configure(): Unit = {
    installModules()
    setupServices()

    expose[ExecutionContext]
    expose[PeopleQueryHttpService]
  }

  private[this] def installModules() = {
    install(new Elastic4sModule)
  }

  private[this] def setupServices(): Unit = {
    bind[ExecutionContext].toInstance(ExecutionContext.Implicits.global)

    bind[Translator[DatatrustMatchRequest, PersonCriteria]].to[DatatrustMatchRequestToPersonCriteria].in[Singleton]
    bind[Translator[Person, DatatrustMatchResponse]].to[PersonToPersonDatatrustResponse].in[Singleton]

    bind[PeopleQuery].to[PeopleQueryService].in[Singleton]
    bind[PeopleQueryServiceRoute].in[Singleton]
    bind[PeopleQueryHttpService].in[Singleton]
  }
}