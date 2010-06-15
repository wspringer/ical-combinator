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

import org.apache.tools.ant.util.ReaderInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UriListHttpMessageConverterTest {

    @Mock
    private HttpInputMessage message;

    @Mock
    private HttpHeaders headers;

    @Test
    public void shouldParseCorrectly() throws IOException {
        UriListHttpMessageConverter converter = new UriListHttpMessageConverter();
        List<URI> list = new ArrayList<URI>();
        when(message.getHeaders()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(message.getBody()).thenReturn(new ReaderInputStream(new StringReader("http://localhost:8080\nhttp://localhost:7070")));
        list = converter.read((Class<? extends List<URI>>) list.getClass(), message);
        assertThat(list.size(), is(2));
    }

    @Test
    public void shouldParseXsltProducedCorrectly() throws IOException {
        Resource resource = new ClassPathResource("scala-tribes.ical");
        UriListHttpMessageConverter converter = new UriListHttpMessageConverter();
        List<URI> list = new ArrayList<URI>();
        when(message.getHeaders()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(message.getBody()).thenReturn(resource.getInputStream());
        list = converter.read((Class<? extends List<URI>>) list.getClass(), message);
        assertThat(list.size(), is(8));
    }


}
