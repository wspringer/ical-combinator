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
import net.fortuna.ical4j.model.ValidationException;
import nl.flotsam.calendar.core.Calendar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryComoundCalendarTest {

    @Mock
    private URLFetchService urlFetchService;

    @Test
    public void shouldParseNingCalendarCorrectly() throws IOException, ValidationException {
        Resource resource = new ClassPathResource("/ning.ical");
        assertTrue(resource.exists());
        URI uri = resource.getURI();


        InMemoryCompoundCalendarRepository repository = new InMemoryCompoundCalendarRepository(urlFetchService);
        Calendar calendar = repository.putCalendar("test", Arrays.asList(uri));
        calendar = repository.getCalendar("test");
        assertThat(calendar, is(not(nullValue())));
    }
}
