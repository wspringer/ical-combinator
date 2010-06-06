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
import nl.flotsam.util.Production;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;

public class URLFetchServiceProduction implements Production<HTTPResponse> {

    private final URL url;

    public URLFetchServiceProduction(URL url) {
        this.url = url;
    }

    public URLFetchServiceProduction(URI uri) throws MalformedURLException {
        this.url = uri.toURL();
    }

    @Override
    public Future<HTTPResponse> produce() {
        return URLFetchServiceFactory.getURLFetchService().fetchAsync(url);
    }

}
