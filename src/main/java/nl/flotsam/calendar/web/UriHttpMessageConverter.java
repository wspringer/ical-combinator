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
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UriHttpMessageConverter extends AbstractHttpMessageConverter<URI> {

    public UriHttpMessageConverter() {
        super(MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return URI.class == clazz;
    }

    @Override
    protected URI readInternal(Class<? extends URI> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        InputStream in = null;
        try {
            in = inputMessage.getBody();
            String uri = IOUtils.toString(in);
            if (inputMessage.getHeaders().getContentType() == MediaType.APPLICATION_FORM_URLENCODED) {
                uri = URLDecoder.decode(uri, "UTF-8");
            }
            return new URI(uri);
        } catch (URISyntaxException urie) {
            throw new HttpMessageNotReadableException("Failed to parse incoming String into a URI.", urie);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    protected void writeInternal(URI uri, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStream out = null;
        try {
            out = outputMessage.getBody();
            if (MediaType.APPLICATION_FORM_URLENCODED == outputMessage.getHeaders().getContentType()) {
                IOUtils.write(URLEncoder.encode(uri.toASCIIString(), "UTF-8"), out, "US-ASCII");
            } else {
                IOUtils.write(uri.toASCIIString(), out, "US-ASCII");
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

}
