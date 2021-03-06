package io.github.crabzilla.vertx.handler.impl

import io.github.crabzilla.core.Command
import io.github.crabzilla.vertx.handler.CommandExecution
import io.github.crabzilla.vertx.handler.CommandHandlerService
import io.github.crabzilla.vertx.projector.ProjectionData
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.CaseInsensitiveHeaders
import org.slf4j.LoggerFactory

class CommandHandlerServiceImpl(private val vertx: Vertx, private val projectionEndpoint: String) :
  CommandHandlerService {

  companion object {

    internal var log = LoggerFactory.getLogger(CommandHandlerServiceImpl::class.java)
  }

  private val commandDeliveryOptions = DeliveryOptions().setCodecName(Command::class.java.simpleName)

  init {
    log.info("will publish resulting events to {}", projectionEndpoint)
  }

  override fun postCommand(handlerEndpoint: String, command: Command,
                           handler: Handler<AsyncResult<CommandExecution>>) {

    log.info("posting a command to {}", handlerEndpoint)

    vertx.eventBus().send<Command>(handlerEndpoint, command, commandDeliveryOptions) { response ->

      if (!response.succeeded()) {
        log.error("postCommand", response.cause())
        handler.handle(Future.failedFuture(response.cause()))
        return@send
      }

      val result = response.result().body() as CommandExecution
      val uow = result.unitOfWork
      val uowSequence = result.uowSequence ?: 0L

      log.info("result = {}", result)

      if (uow != null && uowSequence != 0L) {
        val headers = CaseInsensitiveHeaders().add("uowSequence", uowSequence.toString())
        val eventsDeliveryOptions = DeliveryOptions().setCodecName(ProjectionData::class.simpleName).setHeaders(headers)
        val pd = ProjectionData(uow.unitOfWorkId, uowSequence, uow.targetId().stringValue(), uow.events)
        vertx.eventBus().publish(projectionEndpoint, pd, eventsDeliveryOptions)
      }

      handler.handle(Future.succeededFuture(result))

    }

  }

}
