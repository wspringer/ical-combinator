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
package nl.flotsam.calendar.core.util;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.Calendars;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IcalWriter {

    private static Logger logger = Logger.getLogger(IcalWriter.class.getName());

    public static void writeAsIcal(Writer writer, List<URI> uris) throws IOException {
        Calendar calendar = combine(uris);
        CalendarOutputter outputter = new CalendarOutputter(false);
        try {
            outputter.output(calendar, writer);
        } catch (ValidationException e) {
            logger.log(Level.SEVERE, "Failed to convert calendar into ical.", e);
        }
    }

    public static void writeAsIcal(Writer writer, URI... uris) throws IOException {
        writeAsIcal(writer, Arrays.asList(uris));
    }

    public static Calendar combine(List<URI> uris) {
        Calendar current = new net.fortuna.ical4j.model.Calendar();
        // TODO: Better exception handling
        for (URI uri : uris) {
            try {
                current = Calendars.merge(current, load(uri));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParserException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return current;
    }

    private static Calendar load(URI uri) throws IOException, ParserException {
        UnfoldingReader reader = null;
        try {
            reader = new UnfoldingReader(new InputStreamReader(uri.toURL().openStream()), 3000);
            return new CalendarBuilder().build(reader);
        } finally {
            reader.close();
        }
    }

}
