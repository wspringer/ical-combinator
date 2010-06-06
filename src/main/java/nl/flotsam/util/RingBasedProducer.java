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
package nl.flotsam.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RingBasedProducer implements Producer {

    private final int maxCapacity;
    private static Logger logger = Logger.getLogger(RingBasedProducer.class.getName());

    public RingBasedProducer(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public <T> List<Future<T>> completeAll(Collection<? extends Production<T>> productions) {
        Node<T> current = createRing(maxCapacity);
        List<Future<T>> results = new ArrayList<Future<T>>(productions.size());
        Iterator<? extends Production<T>> iterator = productions.iterator();
        int anticipated = productions.size();
        do {
            anticipated = current.execute(iterator, results, anticipated);
            current = current.getPrevious();
        } while(anticipated > 0);
        return results;
    }

    private <T> Node<T> createRing(int maxCapacity) {
        int i = 0;
        Node<T> first = null;
        Node<T> previous = null;
        while (i < maxCapacity) {
            if (first == null) {
                first = new Node<T>();
                previous = first;
            } else {
                previous = new Node<T>(previous);
            }
            i++;
        }
        first.setPrevious(previous);
        return first;
    }

    private static class Node<T> {

        private Future<T> future;
        private Node<T> previous;

        public Node() {
        }

        public Node(Node<T> previous) {
            this.previous = previous;
        }

        public void setFuture(Future<T> future) {
            this.future = future;
        }

        public Future<T> getFuture() {
            return future;
        }

        public void setPrevious(Node<T> previous) {
            this.previous = previous;
        }

        public int execute(Iterator<? extends Production<T>> iterator, List<Future<T>> results, int anticipated) {
            if (future == null) {
                if (iterator.hasNext()) {
                    Production<T> production = iterator.next();
                    try {
                        future = production.produce();
                        logger.info("Started production.");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to create start production.", e);
                        return anticipated - 1;
                    }
                } // Otherwise we simply skip
            } else {
                if (future.isDone()) {
                    results.add(future);
                    future = null;
                    return anticipated - 1;
                }
            }
            return anticipated;
        }

        public Node<T> getPrevious() {
            return previous;
        }
    }

}
