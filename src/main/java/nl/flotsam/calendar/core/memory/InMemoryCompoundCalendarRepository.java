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
package nl.flotsam.calendar.core.memory;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import nl.flotsam.calendar.core.Calendar;
import nl.flotsam.calendar.core.CalendarRepository;
import nl.flotsam.calendar.core.util.IcalWriter;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class InMemoryCompoundCalendarRepository implements CalendarRepository {

    private final HashMap<String, InMemoryCompoundCalendar> calendars = new HashMap<String, InMemoryCompoundCalendar>();
    private final URLFetchService urlFetchService;

    public InMemoryCompoundCalendarRepository(URLFetchService urlFetchService) {
        this.urlFetchService = urlFetchService;
    }

    @Override
    public Calendar putCalendar(String key, List<URI> feeds) {
        InMemoryCompoundCalendar calendar = new InMemoryCompoundCalendar(feeds, urlFetchService);
        calendars.put(key, calendar);
        return calendar;
    }

    @Override
    public InMemoryCompoundCalendar getCalendar(String key) {
        return calendars.get(key);
    }

    @XStreamAlias("calendar")
    private static class InMemoryCompoundCalendar implements Calendar {

        @XStreamImplicit(itemFieldName = "source")
        private final List<URI> feeds;
        private final URLFetchService urlFetchService;

        public InMemoryCompoundCalendar(List<URI> feeds, URLFetchService urlFetchService) {
            this.feeds = feeds;
            this.urlFetchService = urlFetchService;
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
            IcalWriter.writeAsIcal(writer, urlFetchService, feeds);
        }

    }

}
