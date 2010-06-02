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

import nl.flotsam.calendar.core.CalendarClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@Ignore
public class RemoteCompoundCalendarIntegrationTest {

    private static final String BASE_URI = "http://ical-combinator.appspot.com/";

    @Test
    public void shouldAcceptPostingSource() throws URISyntaxException {
        CalendarClient client = new CalendarClient(new URI(BASE_URI));
        client.putCalendar("test");
    }

}
