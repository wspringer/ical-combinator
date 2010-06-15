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
package nl.flotsam.calendar.core.cache;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import nl.flotsam.calendar.core.Calendar;
import nl.flotsam.calendar.core.CalendarRepository;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class CachingCalendarRepository implements CalendarRepository {

    private final MemcacheService memcache;
    private final CalendarRepository repository;
    private final Expiration expiration;
    private final static Logger logger = Logger.getLogger(CachingCalendarRepository.class.getName());

    @Autowired
    public CachingCalendarRepository(MemcacheService memcache, CalendarRepository delegate, Expiration expiration) {
        this.memcache = memcache;
        this.repository = delegate;
        this.expiration = expiration;
    }

    @Override
    public Calendar putCalendar(String key, List<URI> feeds) {
        Calendar result = repository.putCalendar(key, feeds);
        CacheableCalendar cachable = createCacheableCalendar(result);
        memcache.put(key, cachable, expiration);
        return cachable;
    }

    @Override
    public Calendar getCalendar(String key) {
        CacheableCalendar result = (CacheableCalendar) memcache.get(key);
        if (result == null) {
            logger.info("Repopulating calendar " + key + " from cache.");
            result = createCacheableCalendar(repository.getCalendar(key));
            memcache.put(key, result, expiration);
        } else {
            logger.info("Serving calendar " + key + " from cache.");
        }
        return result;
    }

    private static CacheableCalendar createCacheableCalendar(Calendar calendar) {
        StringWriter writer = new StringWriter();
        try {
            calendar.toIcal(writer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create iCal representation.");
        }
        return new CacheableCalendar(calendar.getFeeds(),
                calendar.getLastTimeUpdated(), 
                calendar.isStale(),
                writer.toString());
    }

    private static class CacheableCalendar implements Calendar, Serializable {

        private final List<URI> feeds;
        private final LocalDate lastTimeUpdated;
        private final boolean stale;
        private final String icalRepresentation;

        public CacheableCalendar(List<URI> feeds, LocalDate lastTimeUpdated, boolean stale, String icalRepresentation) {
            this.feeds = feeds;
            this.lastTimeUpdated = lastTimeUpdated;
            this.stale = stale;
            this.icalRepresentation = icalRepresentation;
        }

        @Override
        public List<URI> getFeeds() {
            return feeds;
        }

        @Override
        public LocalDate getLastTimeUpdated() {
            return lastTimeUpdated;
        }

        @Override
        public boolean isStale() {
            return stale;
        }

        @Override
        public void toIcal(Writer writer) throws IOException {
            writer.write(icalRepresentation);
        }
    }
}
