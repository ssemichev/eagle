package eagle.peopleapi.rest.IoC

import java.lang.annotation.Annotation

import com.google.inject.Guice
import com.google.inject.Stage
import eagle.common.traits.ApplicationGlobalBase
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

object Global extends ApplicationGlobalBase {

  lazy val injector = Guice.createInjector(Stage.PRODUCTION, new Bootstrapper)

  override def getInstance[T: Manifest]: T = injector.instance[T]

  override def getInstance[T: Manifest](ann: Annotation): T = injector.instance[T](ann)
}
