package eagle.peopleapi.service.datatrust

import eagle.peopleapi.service.datatrust.models.Person
import eagle.peopleapi.service.datatrust.models.PersonCriteria

import scala.concurrent.Future

trait PeopleQuery {
  def getById(id: String): Future[Option[Person]]
  def findBy(criteria: PersonCriteria): Future[Array[Person]]
}
