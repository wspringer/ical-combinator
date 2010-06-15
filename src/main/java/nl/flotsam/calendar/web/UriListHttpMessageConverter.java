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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UriListHttpMessageConverter extends AbstractHttpMessageConverter<List<URI>> {

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected List<URI> readInternal(Class<? extends List<URI>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        InputStream in = inputMessage.getBody();
        try {
            String text = IOUtils.toString(in, "UTF-8");
            List<URI> result = parseURIs(text);
            logger.info("Produced a list of " + result.size() + " items: " + result);
            return result;
        } catch (URISyntaxException e) {
            throw new HttpMessageNotReadableException("Illegal URI in list of URIs: " + e.getInput());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    protected void writeInternal(List<URI> uris, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        Writer writer = new OutputStreamWriter(outputMessage.getBody(), "UTF-8");
        try {
            for (int i = 0; i < uris.size(); i++) {
                if (i != 0) {
                    writer.write('\n');
                }
                writer.write(uris.get(i).toASCIIString());
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.TEXT_PLAIN);
    }

    private List<URI> parseURIs(String text) throws URISyntaxException {
        String[] lines = text.split("\\r?\\n");
        List<URI> uris = new ArrayList<URI>();
        for (String line : lines) {
            if (!StringUtils.isEmpty(line)) {
                uris.add(new URI(line));
            }
        }
        return uris;
    }

}