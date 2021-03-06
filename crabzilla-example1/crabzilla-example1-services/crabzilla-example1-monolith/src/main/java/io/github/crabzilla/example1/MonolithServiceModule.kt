package io.github.crabzilla.example1

import dagger.Module
import dagger.Provides
import io.github.crabzilla.vertx.*
import io.github.crabzilla.vertx.handler.CommandHandlerService
import io.github.crabzilla.vertx.handler.impl.CommandHandlerServiceImpl
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.HealthCheckHandler
import javax.inject.Singleton

@Module(includes = [CrabzillaWebModule::class, Example1HandlerModule::class, Example1ProjectorModule::class])
class MonolithServiceModule(vertx: Vertx, config: JsonObject) : CrabzillaModule(vertx, config) {

  @Provides
  @Singleton
  fun handlerService() : CommandHandlerService {
    return CommandHandlerServiceImpl(vertx, subDomainName())
  }

  @Provides
  @Singleton
  fun restVerticle(uowRepository: UnitOfWorkRepository, config: JsonObject,
                   handlerService: CommandHandlerService,
                   @WebHealthCheck healthCheckHandler: HealthCheckHandler): CrabzillaRestVerticle {
    return CrabzillaRestVerticle(subDomainName(), config, healthCheckHandler, uowRepository, handlerService)
  }

}
