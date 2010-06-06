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

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.Calendars;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        List<Future<HTTPResponse>> futures = createFuturesFrom(uris);
        for (Future<HTTPResponse> future : futures) {
            try {
                current = merge(current, future.get());
            } catch (InterruptedException e) {
                logger.warning("Ignoring failed request");
            } catch (ExecutionException e) {
                logger.log(Level.WARNING, "Request failed.", e.getCause());
            }
        }
        return current;
    }

    private static List<Future<HTTPResponse>> createFuturesFrom(List<URI> uris) {
        List<Future<HTTPResponse>> results = new ArrayList<Future<HTTPResponse>>(uris.size());
        for (URI uri : uris) {
            Future<HTTPResponse> future = createFutureFrom(uri);
            if (future != null) {
                results.add(future);
            }
        }
        return results;
    }

    private static Future<HTTPResponse> createFutureFrom(URI uri) {
        try {
            return URLFetchServiceFactory.getURLFetchService().fetchAsync(uri.toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static Calendar merge(Calendar current, HTTPResponse response) {
        try {
            return Calendars.merge(current, loadFrom(response.getContent()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load content from response.");
            return current;
        } catch (ParserException e) {
            logger.log(Level.WARNING, "Failed to parse Calendar.");
            return current;
        }
    }

    private static Calendar loadFrom(byte[] content) throws IOException, ParserException {
        return loadFrom(new ByteArrayInputStream(content));
    }

    private static Calendar loadFrom(InputStream in) throws IOException, ParserException {
        UnfoldingReader reader = null;
        try {
            reader = new UnfoldingReader(new InputStreamReader(in), 3000);
            return new CalendarBuilder().build(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

}
