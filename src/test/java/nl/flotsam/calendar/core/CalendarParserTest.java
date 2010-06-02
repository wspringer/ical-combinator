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
package nl.flotsam.calendar.core;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertThat;

public class CalendarParserTest {

    @Test
    public void shouldParseMeetupICalCorrectly() throws IOException, ParserException {
        UnfoldingReader reader = new UnfoldingReader(new InputStreamReader(new ClassPathResource("test.ical").getInputStream(), "UTF-8"), 3000);
        Calendar calendar = new CalendarBuilder().build(reader);
    }

    @Test
    public void shouldUnfoldCorrectly() throws IOException {
        UnfoldingReader reader = new UnfoldingReader(new InputStreamReader(new ClassPathResource("sample-single-line.ical").getInputStream(), "UTF-8"), 3000);
        IOUtils.copy(reader, System.out);
    }

}
