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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.Calendars;
import nl.flotsam.util.Producer;
import nl.flotsam.util.Production;
import nl.flotsam.util.RingBasedProducer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
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
        List<Production<HTTPResponse>> productions = prepareProductions(uris);
        Producer producer = new RingBasedProducer(10); // max 10 requests handled at a time
        List<Future<HTTPResponse>> futures = producer.completeAll(productions);
        for (Future<HTTPResponse> future : futures) {
            try {
                HTTPResponse response = future.get();
                if (response.getResponseCode() == 200) {
                    current = merge(current, response);
                } else {
                    logger.log(Level.WARNING, "Non-succesful download for " + response.getFinalUrl());
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (ExecutionException e) {
                logger.log(Level.WARNING, "Failed to complete download.", e.getCause());
            }
        }
        return current;
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

    private static List<Production<HTTPResponse>> prepareProductions(List<URI> uris) {
        List<Production<HTTPResponse>> productions =
                Lists.transform(uris, new Function<URI, Production<HTTPResponse>>() {
                    @Override
                    public Production<HTTPResponse> apply(URI uri) {
                        try {
                            return new URLFetchServiceProduction(uri);
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }
                });
        // Take out all null elements
        return Lists.newArrayList(Iterators.filter(productions.iterator(), new Predicate<Production<HTTPResponse>>() {
            @Override
            public boolean apply(Production<HTTPResponse> input) {
                return input != null;
            }
        }));
    }

    private static Calendar loadFrom(byte[] content) throws IOException, ParserException {
        return loadFrom(new ByteArrayInputStream(content));
    }

    private static Calendar loadFrom(URI uri) throws IOException, ParserException {
        return loadFrom(uri.toURL().openStream());
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
