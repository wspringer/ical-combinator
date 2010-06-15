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
import nl.flotsam.calendar.core.Calendar;
import nl.flotsam.test.WebResource;
import nl.flotsam.test.WebResourceServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryCompoundCalendarRepositoryIntegrationTest {

    @Mock
    private URLFetchService urlFetchService;
    
    @Rule
    public WebResourceServer server = new WebResourceServer("TEST_HTTP_PORT", 9009);

    @Test
    @WebResource(content = "classpath:meetup-sample.ical", contentType = "text/calendar")
    public void shouldRetrieveCalendarCorrectly() throws URISyntaxException {
        URI uri = new URI(server.getURL());
        InMemoryCompoundCalendarRepository repo = new InMemoryCompoundCalendarRepository(urlFetchService);
        Calendar calendar = repo.putCalendar("test", Arrays.asList(uri));
        calendar = repo.getCalendar("test");
        assertThat(calendar, is(not(nullValue())));
    }

}
