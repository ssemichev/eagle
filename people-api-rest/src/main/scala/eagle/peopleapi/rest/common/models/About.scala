package eagle.peopleapi.rest.common.models

import akka.http.scaladsl.model.DateTime

final case class About(serviceName: String, currentUtc: DateTime, version: String, builtAt: String)
