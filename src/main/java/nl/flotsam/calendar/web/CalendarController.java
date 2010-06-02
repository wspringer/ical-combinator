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

import net.fortuna.ical4j.model.ValidationException;
import nl.flotsam.calendar.core.Calendar;
import nl.flotsam.calendar.core.CalendarRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class CalendarController {

    private final static Logger logger = Logger.getLogger(CalendarController.class.getName());

    private final CalendarRepository repository;

    private static final MediaType ICAL_CONTENT_TYPE = new MediaType("text", "calendar");

    @Autowired
    public CalendarController(CalendarRepository repository) {
        this.repository = repository;
    }

    @ExceptionHandler(URISyntaxException.class)
    public void handleURISyntaxException(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid URI passed as a parameter.");
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/calendars/{key}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCalendar(@PathVariable("key") String key, @RequestBody List<URI> feeds, HttpServletRequest request)
            throws URISyntaxException, MalformedURLException
    {
        logger.info("Creating calendar for key '" + key + "' and feeds: " + StringUtils.join(feeds, ", "));
        Calendar calendar = repository.putCalendar(key, feeds);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/calendars/{key}")
    public ModelAndView getCalendarAsIcal(@PathVariable("key") String key,
                                          @RequestHeader("Accept") String acceptHeader,
                                          HttpServletRequest request)
            throws ValidationException, IOException, NoSuchRequestHandlingMethodException
    {
        Calendar calendar = repository.getCalendar(key);
        if (calendar == null) {
            throw new NoSuchRequestHandlingMethodException(request);
        }
        if (acceptHeader.contains("text/calendar")) {
            return new ModelAndView("icalView", BindingResult.MODEL_KEY_PREFIX + "calendar", calendar);
        } else {
            return new ModelAndView("xmlView", BindingResult.MODEL_KEY_PREFIX + "calendar", calendar);
        }
    }

//    private URI extractURI(String id, String pathInfo) throws URISyntaxException {
//        URI uri = new URI(pathInfo.substring(pathInfo.lastIndexOf(id) + id.length() + 1));
//        if (!uri.isAbsolute()) {
//            throw new URISyntaxException(uri.toASCIIString(), "Expecting absolute URI.");
//        } else {
//            return uri;
//        }
//    }

}
