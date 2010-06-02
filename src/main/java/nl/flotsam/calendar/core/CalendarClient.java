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

import nl.flotsam.calendar.web.UriListHttpMessageConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CalendarClient {

    private final URI baseURI;

    private final RestTemplate template;

    private Logger logger = Logger.getLogger(CalendarClient.class.getName());

    public CalendarClient(URI baseURI) {
        this.baseURI = baseURI;
        template = new RestTemplate();
        template.setMessageConverters(Arrays.asList(new HttpMessageConverter<?>[]{
                new UriListHttpMessageConverter(),
                new StringHttpMessageConverter()
        }));
        final ClientHttpRequestFactory factory = template.getRequestFactory();
        template.setRequestFactory(new ClientHttpRequestFactory() {

            @Override
            public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
                logger.info("Sending " + httpMethod.name() + " to " + uri.toASCIIString());
                return factory.createRequest(uri, httpMethod);
            }
        });
    }

    public void putCalendar(String key, URI... uris) {
        String address = UriBuilder.fromUri(baseURI).path("calendars").build().toASCIIString() + "/{key}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key", key);
        template.put(address, Arrays.asList(uris), params);
    }

    public String getCalendarAsIcal(String key) {
        return getCalendarAsType(key, "text/calendar");
    }

    public String getCalendarAsXml(String key) {
        return getCalendarAsType(key, "text/xml");
    }

    public String getCalendarAsType(String key, String contentType) {
        String address = UriBuilder.fromUri(baseURI).path("calendars").build().toASCIIString() + "/{key}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key", key);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", contentType);
        HttpEntity<?> request = new HttpEntity(headers);
        HttpEntity<String> response =
                template.exchange(address, HttpMethod.GET, request, String.class, params);
        return response.getBody();
    }



    private String getPayloadAsText(URI[] uris) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < uris.length; i++) {
            if (i != 0) {
                builder.append("\n");
            }
            builder.append(uris[i].toASCIIString());
        }
        return builder.toString();
    }

}
