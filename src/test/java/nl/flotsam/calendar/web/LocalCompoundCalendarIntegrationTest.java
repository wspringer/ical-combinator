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
package nl.flotsam.calendar.web;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import nl.flotsam.calendar.core.CalendarClient;
import nl.flotsam.test.WebResource;
import nl.flotsam.test.WebResourceServer;
import nl.flotsam.test.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class LocalCompoundCalendarIntegrationTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalMemcacheServiceTestConfig());

    @Rule
    public WebServer server = new WebServer("TEST_HTTP_PORT", 9009, this.getClass().getClassLoader());

    @Rule
    public WebResourceServer remoteServer = new WebResourceServer("REMOTE_HTTP_PORT", 9010);

    @Before
    public void setUp() {
        final Semaphore started = new Semaphore(1);
        server.setBefore(new Runnable() {
            @Override
            public void run() {
                if (started.tryAcquire()) {
                    helper.setUp();
                }
            }
        });
    }

    @After
    public void tearDown() {
        helper.tearDown(); 
    }

    @Test
    public void shouldAcceptCreationOfCalendar() throws URISyntaxException {
        CalendarClient client = new CalendarClient(getBaseURI());
        client.putCalendar("test", new URI(remoteServer.getURL()));
    }

    @Test
    @WebResource(content = "classpath:meetup-sample.ical", contentType = "text/calendar")
    public void shouldReturnResultsAsICal() throws URISyntaxException {
        CalendarClient client = new CalendarClient(getBaseURI());
        client.putCalendar("test", new URI(remoteServer.getURL()));
        String ical = client.getCalendarAsIcal("test");
        assertThat(ical, containsString("SUMMARY:The Boulder Area Scala Enthusiasts Monthly Meetup"));
    }

    @Test
    @WebResource(content = "classpath:meetup-sample.ical", contentType = "text/plain")
    public void shouldReturnResultsAsText() throws URISyntaxException {
        CalendarClient client = new CalendarClient(getBaseURI());
        client.putCalendar("test", new URI(remoteServer.getURL()));
        System.err.println(client.getCalendarAsText("test"));
    }

    protected URI getBaseURI() {
        return server.getBaseURI();
    }

}
