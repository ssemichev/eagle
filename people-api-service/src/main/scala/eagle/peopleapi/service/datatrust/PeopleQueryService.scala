package eagle.peopleapi.service.datatrust

import javax.inject.Inject
import javax.inject.Named

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import eagle.peopleapi.service._
import eagle.peopleapi.service.common.Constants._
import eagle.peopleapi.service.datatrust.models.Person
import eagle.peopleapi.service.datatrust.models.PersonCriteria

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PeopleQueryService @Inject() (
    @Named(voterDataCluster) client:         ElasticClient,
    @Named(peopleIndexAndType) indexAndType: IndexAndType
)(implicit ec: ExecutionContext) extends PeopleQuery {

  override def getById(entityId: String): Future[Option[Person]] = {
    client.execute {
      get id entityId from indexAndType
    } map (_.as[Person])
  }

  override def findBy(criteria: PersonCriteria): Future[Array[Person]] = {

    val mustFirstName = criteria.firstName.map { firstName ⇒
      matchQuery("name.first_name.hash", firstName).fuzziness(2).prefixLength(1)
    }

    val mustLastName = criteria.lastName.map { lastName ⇒
      bool(
        should(
          {
            matchQuery("name.last_name.hash", lastName).fuzziness(2).prefixLength(1).boost(3.0)
          }, {
            matchQuery("name.last_name_with_suffix", lastName).fuzziness(2).prefixLength(1).boost(4.0)
          }
        )
      )
    }

    val mustZipCodes: Option[QueryDefinition] = {
      Seq(criteria.regZip, criteria.mailZip).flatten.headOption map { _ ⇒
        bool(
          should(
            Seq(
              criteria.regZip.map(zip ⇒ termQuery("reg_zip", zip).boost(7.0)),
              criteria.mailZip.map(zip ⇒ termQuery("mail_zip", zip).boost(5.0))
            ).flatten: _*
          )
        )
      }
    }

    val searchQuery = bool(
      must(
        {
          Seq(mustFirstName, mustLastName, mustZipCodes).flatten: _*
        }
      )
    )

    client.execute {
      search in indexAndType query {
        searchQuery
      } limit criteria.limit
    } map (_.asArray[Person])
  }
}