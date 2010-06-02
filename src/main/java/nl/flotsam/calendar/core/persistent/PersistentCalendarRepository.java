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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import nl.flotsam.calendar.core.Calendar;
import nl.flotsam.calendar.core.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class PersistentCalendarRepository implements CalendarRepository {

    private final DatastoreService store;

    private static final Logger logger = Logger.getLogger(PersistentCalendarRepository.class.getName());

    @Autowired
    public PersistentCalendarRepository(DatastoreService store) {
        this.store = store;
    }

    @Override
    public Calendar putCalendar(String calendarKey, List<URI> feeds) {
        PersistentCalendar result = new PersistentCalendar(feeds);
        store.put(result.toEntity(calendarKey));
        return result;
    }

    @Override
    public Calendar getCalendar(String key) {
        try {
            Entity entity = store.get(PersistentCalendar.getDataStoreCalendarKey(key));
            return PersistentCalendar.fromEntity(entity);
        } catch (EntityNotFoundException enfe) {
            logger.severe("Failed to find calendar for key " + key);
            return null;
        }
    }

}

