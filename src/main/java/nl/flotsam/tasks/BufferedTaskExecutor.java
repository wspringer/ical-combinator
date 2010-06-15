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
package nl.flotsam.tasks;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link nl.flotsam.tasks.TaskExecutor} that will buffer tasks executing in a limited-capacity queue, waiting for the
 * first one send in to complete before waiting for the second, etc.
 */
public class BufferedTaskExecutor implements TaskExecutor {

    /**
     * The size of the buffer.
     */
    private final int capacity;

    /**
     * The object called in case task execution fails.
     */
    private final TaskExcecutionFailureHandler handler;

    private final static Logger logger = Logger.getLogger(BufferedTaskExecutor.class.getName());

    public BufferedTaskExecutor(int capacity, TaskExcecutionFailureHandler handler) {
        this.capacity = capacity;
        this.handler = handler;
    }

    public BufferedTaskExecutor(int capacity) {
        this(capacity, new TaskExcecutionFailureHandler() {

            @Override
            public boolean onException(InterruptedException e) {
                logger.log(Level.WARNING, "Exception while producing results.", e);
                return true;
            }

            @Override
            public boolean onException(ExecutionException e) {
                logger.log(Level.WARNING, "Failed to start results.", e);
                return true;
            }

        });
    }

    @Override
    public <T> void execute(Iterable<? extends Task<? extends T>> productions,
                            Collection<? super T> results) {
        Queue<Future<? extends T>> buffer = new LinkedList<Future<? extends T>>();
        int i = 0;
        Iterator<? extends Task<? extends T>> productionIterator = productions.iterator();
        while (i < capacity && productionIterator.hasNext()) {
            buffer.offer(productionIterator.next().start());
            i++;
        }
        Future<? extends T> future = null;
        while ((future = buffer.poll()) != null) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                if (!handler.onException(e)) break;
            } catch (ExecutionException e) {
                if (!handler.onException(e)) break;
            }
            if (productionIterator.hasNext()) {
                buffer.offer(productionIterator.next().start());
            }
        }

    }

}
