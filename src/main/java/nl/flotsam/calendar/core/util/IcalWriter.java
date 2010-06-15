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
import com.google.appengine.api.urlfetch.URLFetchService;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.Calendars;
import nl.flotsam.tasks.BufferedTaskExecutor;
import nl.flotsam.tasks.Task;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IcalWriter {

    private static Logger logger = Logger.getLogger(IcalWriter.class.getName());
    private static final String ICALENDAR_ENCODING = "UTF-8";

    public static void writeAsIcal(Writer writer, Calendar calendar) throws IOException {
        CalendarOutputter outputter = new CalendarOutputter(false);
        try {
            outputter.output(calendar, writer);
        } catch (ValidationException e) {
            logger.log(Level.SEVERE, "Failed to convert calendar into ical.", e);
        }
    }

    public static void writeAsIcal(Writer writer, URLFetchService urlFetchService, List<URI> uris) throws IOException {
        writeAsIcal(writer, combine(uris));
    }

    public static void writeAsIcal(Writer writer, URLFetchService urlFetchService, URI... uris) throws IOException {
        writeAsIcal(writer, urlFetchService, Arrays.asList(uris));
    }

    public static Calendar combine(List<URI> uris) {
        Calendar current = new net.fortuna.ical4j.model.Calendar();
        List<Task<HTTPResponse>> tasks = createTasksFrom(uris);
        List<HTTPResponse> results = new LinkedList<HTTPResponse>();
        new BufferedTaskExecutor(10).execute(tasks, results);
        for (HTTPResponse response : results) {
            current = merge(current, response);
        }
        return current;
    }

    private static List<Task<HTTPResponse>> createTasksFrom(List<URI> uris) {
        List<Task<HTTPResponse>> tasks = new LinkedList<Task<HTTPResponse>>();
        for (URI uri : uris) {
            try {
                tasks.add(new URLFetchServiceTask(uri));
            } catch (MalformedURLException e) {
                logger.warning("Skipping " + uri + ", since it's not a URL.");
            }
        }
        return tasks;
    }

    private static List<Future<HTTPResponse>> createFuturesFrom(List<URI> uris, URLFetchService urlFetchService) {
        List<Future<HTTPResponse>> results = new ArrayList<Future<HTTPResponse>>(uris.size());
        for (URI uri : uris) {
            Future<HTTPResponse> future = createFutureFrom(uri, urlFetchService);
            if (future != null) {
                results.add(future);
            }
        }
        return results;
    }

    private static Future<HTTPResponse> createFutureFrom(URI uri, URLFetchService urlFetchService) {
        try {
            return urlFetchService.fetchAsync(uri.toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static Calendar merge(Calendar current, HTTPResponse response) {
        if (response.getResponseCode() == 200) {
            try {
                return Calendars.merge(current, loadFrom(response.getContent()));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to load content from response.");
                return current;
            } catch (ParserException e) {
                logger.log(Level.WARNING, "Failed to parse Calendar.");
                return current;
            }
        } else {
            return current;
        }
    }

    private static Calendar loadFrom(byte[] content) throws IOException, ParserException {
        return loadFrom(new ByteArrayInputStream(content));
    }

    private static Calendar loadFrom(InputStream in) throws IOException, ParserException {
        UnfoldingReader reader = null;
        try {
            reader = new UnfoldingReader(new InputStreamReader(in, ICALENDAR_ENCODING), 3000);
            return new CalendarBuilder().build(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

}
