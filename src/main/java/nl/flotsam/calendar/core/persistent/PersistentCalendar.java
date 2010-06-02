/**
 * Copyright (C) 2009-2010 Wilfred Springer
 *
 * This file is part of ICal Combinator.
 *
 * ICal Combinator is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2, or (at
 * your option) any later version.
 *
 * ICal Combinator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Preon; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 */
package nl.flotsam.calendar.core.persistent;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import nl.flotsam.calendar.core.Calendar;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.flotsam.calendar.core.util.IcalWriter.writeAsIcal;


public class PersistentCalendar implements Calendar {

    private final static Logger logger = Logger.getLogger(PersistentCalendar.class.getName());
    private final static String PROPERTY_NAME_URIS = "feeds";

    private final List<URI> feeds;
    private static final String KIND_CALENDAR = "calendar";
    private static final String SEPARATOR = "\n";

    public PersistentCalendar(List<URI> feeds) {
        this.feeds = feeds;
    }

    @Override
    public List<URI> getFeeds() {
        return feeds;
    }

    @Override
    public LocalDate getLastTimeUpdated() {
        return new LocalDate();
    }

    @Override
    public boolean isStale() {
        return false;
    }

    @Override
    public void toIcal(Writer writer) throws IOException {
        writeAsIcal(writer, feeds);
    }

    public static Calendar fromEntity(Entity entity) {
        Object value = entity.getProperty(PROPERTY_NAME_URIS);
        if (value instanceof String) {
            return new PersistentCalendar(stringToUriList((String) value));
        } else {
            return new PersistentCalendar(Collections.<URI>emptyList());
        }
    }

    public Entity toEntity(String calendarKey) {
        Key key = getDataStoreCalendarKey(calendarKey);
        Entity entity = new Entity(key);
        entity.setProperty(PROPERTY_NAME_URIS, uriListToString(feeds));
        return entity;
    }

    public static Key getDataStoreCalendarKey(String calendarKey) {
        return KeyFactory.createKey(KIND_CALENDAR, calendarKey);
    }

    public static String uriListToString(List<URI> value) {
        return StringUtils.join(value, SEPARATOR);
    }

    public static List<URI> stringToUriList(String value) {
        String[] parts = value.split(SEPARATOR);
        List<URI> uris = new ArrayList<URI>(parts.length);
        for (String part : parts) {
            try {
                uris.add(new URI(part));
            } catch (URISyntaxException e) {
                logger.log(Level.SEVERE, "Failed to convert String to URI: " + part);
            }
        }
        return uris;
    }

}
