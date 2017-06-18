/*
 * This file is generated by jOOQ.
*/
package example1.datamodel.tables;


import example1.datamodel.Example1db;
import example1.datamodel.Keys;
import example1.datamodel.tables.records.EventsChannelsRecord;
import org.jooq.*;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class EventsChannels extends TableImpl<EventsChannelsRecord> {

    private static final long serialVersionUID = 713118034;

    /**
     * The reference instance of <code>example1db.events_channels</code>
     */
    public static final EventsChannels EVENTS_CHANNELS = new EventsChannels();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EventsChannelsRecord> getRecordType() {
        return EventsChannelsRecord.class;
    }

    /**
     * The column <code>example1db.events_channels.channel_name</code>.
     */
    public final TableField<EventsChannelsRecord, String> CHANNEL_NAME = createField("channel_name", org.jooq.impl.SQLDataType.VARCHAR.length(36).nullable(false), this, "");

    /**
     * The column <code>example1db.events_channels.uow_last_seq</code>.
     */
    public final TableField<EventsChannelsRecord, Long> UOW_LAST_SEQ = createField("uow_last_seq", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>example1db.events_channels</code> table reference
     */
    public EventsChannels() {
        this("events_channels", null);
    }

    /**
     * Create an aliased <code>example1db.events_channels</code> table reference
     */
    public EventsChannels(String alias) {
        this(alias, EVENTS_CHANNELS);
    }

    private EventsChannels(String alias, Table<EventsChannelsRecord> aliased) {
        this(alias, aliased, null);
    }

    private EventsChannels(String alias, Table<EventsChannelsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Example1db.EXAMPLE1DB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<EventsChannelsRecord> getPrimaryKey() {
        return Keys.KEY_EVENTS_CHANNELS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<EventsChannelsRecord>> getKeys() {
        return Arrays.<UniqueKey<EventsChannelsRecord>>asList(Keys.KEY_EVENTS_CHANNELS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventsChannels as(String alias) {
        return new EventsChannels(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public EventsChannels rename(String name) {
        return new EventsChannels(name, null);
    }
}