package io.github.crabzilla.example1.customer

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.github.crabzilla.core.entity.Entity
import io.github.crabzilla.core.entity.Snapshot
import io.github.crabzilla.core.entity.SnapshotPromoter
import io.github.crabzilla.core.entity.StateTransitionsTracker
import io.github.crabzilla.example1.SampleInternalService
import io.github.crabzilla.vertx.entity.EntityCommandHandlerVerticle
import io.github.crabzilla.vertx.entity.EntityCommandRestVerticle
import io.github.crabzilla.vertx.entity.EntityUnitOfWorkRepository
import io.github.crabzilla.vertx.modules.qualifiers.WriteDatabase
import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import net.jodah.expiringmap.ExpiringMap
import javax.inject.Singleton


// tag::module[]
@Module
class CustomerModule(val vertx: Vertx, val config: JsonObject) {

  @Provides @IntoSet
  fun restVerticle(uowRepository: EntityUnitOfWorkRepository): EntityCommandRestVerticle<out Any> {
    return EntityCommandRestVerticle(Customer::class.java, config, uowRepository)
  }

  @Provides @IntoSet
  fun handlerVerticle(service: SampleInternalService, eventRepository: EntityUnitOfWorkRepository):
          EntityCommandHandlerVerticle<out Entity> {

    val customer = Customer(sampleInternalService = service)
    val stateTransitionFn = StateTransitionFn()
    val validator =  CommandValidatorFn()

    val cache: ExpiringMap<String, Snapshot<Customer>> = ExpiringMap.create()
    val circuitBreaker = CircuitBreaker.create("command-handler-circuit-breaker", vertx)
    val trackerFactory : (Snapshot<Customer>) -> StateTransitionsTracker<Customer> =
            { instance -> StateTransitionsTracker(instance, stateTransitionFn)
            }
    val cmdHandler = CommandHandlerFn(trackerFactory)
    val snapshotPromoter =
            SnapshotPromoter<Customer> { instance -> StateTransitionsTracker(instance, stateTransitionFn) }
    return EntityCommandHandlerVerticle(Customer::class.java, customer, cmdHandler, validator, snapshotPromoter,
            eventRepository, cache, circuitBreaker)
  }

  @Provides
  @Singleton
  fun uowRepo(@WriteDatabase jdbcClient: JDBCClient): EntityUnitOfWorkRepository {
    return EntityUnitOfWorkRepository(Customer::class.java, jdbcClient)
  }

}
// end::module[]