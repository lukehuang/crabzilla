package io.github.crabzilla.vertx.projection;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.crabzilla.core.DomainEvent;
import io.github.crabzilla.vertx.ProjectionData;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.crabzilla.vertx.helpers.VertxSqlHelper.queryStreamWithParams;
import static io.vertx.core.json.Json.mapper;
import static org.slf4j.LoggerFactory.getLogger;

public class ProjectionRepository {

  static Logger log = getLogger(ProjectionRepository.class);

  private final JDBCClient client;

  private final TypeReference<List<DomainEvent>> eventsListTpe = new TypeReference<List<DomainEvent>>() {
  };

  public ProjectionRepository(JDBCClient client) {
    this.client = client;
  }

  public void selectAfterUowSequence(final Long uowSequence,
                                     final Future<List<ProjectionData>> selectAfterUowSeq) {
    selectAfterUowSequence(uowSequence, 100, selectAfterUowSeq);
  }

  public void selectAfterUowSequence(final Long uowSequence, final Integer maxRows,
                   final Future<List<ProjectionData>> selectAfterUowSeq) {

    log.info("will load after uowSequence [{}]", uowSequence);

    JsonArray params = new JsonArray().add(uowSequence);

    String SELECT_AFTER_UOW_SEQ =
            "select uow_id, uow_seq_number, ar_id as target_id, uow_events " +
            "  from units_of_work " +
            " where uow_seq_number > ? " +
            " order by uow_seq_number " +
            " limit " + maxRows
            ;

    client.getConnection(getConn -> {
      if (getConn.failed()) {
        selectAfterUowSeq.fail(getConn.cause());
        return;
      }

      SQLConnection sqlConn = getConn.result();
      Future<SQLRowStream> streamFuture = Future.future();
      queryStreamWithParams(sqlConn, SELECT_AFTER_UOW_SEQ, params, streamFuture);

      streamFuture.setHandler(ar -> {

        if (ar.failed()) {
          selectAfterUowSeq.fail(ar.cause());
          log.error("when scanning for projectionData after uowSequence " + uowSequence, ar.cause());
          return;
        }

        SQLRowStream stream = ar.result();
        ArrayList<ProjectionData> list = new ArrayList<>();
        stream
                .resultSetClosedHandler(v -> {
                  // will ask to restart the stream with the new result set if any
                  stream.moreResults();
                })
                .handler(row -> {
                  final UUID _uowId = UUID.fromString(row.getString(0));
                  final Long _uowSequence = row.getLong(1);
                  final String _targetId = row.getString(2);
                  final List<DomainEvent> _events = readEvents(row.getString(3));

                  final ProjectionData projectionData = new ProjectionData(_uowId, _uowSequence, _targetId, _events);
                  list.add(projectionData);

                }).endHandler(event -> {

          selectAfterUowSeq.complete(list);

          log.info("found {} instances of projectionData after uowSequence {}",
                  list.size(), uowSequence);

          sqlConn.close(done -> {

            if (done.failed()) {
              log.error("when closing sql connection", done.cause());
            }

          });

        });

      });

    });
  }

  private List<DomainEvent> readEvents(String eventsAsJson) {
    try {
      log.info("eventsAsJson {}", eventsAsJson);
      return mapper.readerFor(eventsListTpe).readValue(eventsAsJson);
    } catch (IOException e) {
      throw new RuntimeException("When reading events list from JSON", e);
    }
  }

}