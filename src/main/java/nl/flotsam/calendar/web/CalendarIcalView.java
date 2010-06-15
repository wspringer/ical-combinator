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

import nl.flotsam.calendar.core.Calendar;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.Map;

public class CalendarIcalView extends AbstractView {

    private static final String CONTENT_TYPE = "text/calendar";

    public CalendarIcalView() {
        setContentType(CONTENT_TYPE);
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Calendar calendar = getCalendarToBeRendered(model);
        if (calendar == null) {
            throw new ServletException("Failed to locate Calendar to be rendered.");
        }
        response.setContentType("text/calendar");
        // TODO: We can leverage data from the Calendar here
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        Writer writer = response.getWriter();
        calendar.toIcal(writer);
        writer.flush();
    }

    private Calendar getCalendarToBeRendered(Map<String, Object> model) {
        for (Object value : model.values()) {
            if (value instanceof Calendar) {
                return (Calendar) value;
            }
        }
        return null;
    }

}
