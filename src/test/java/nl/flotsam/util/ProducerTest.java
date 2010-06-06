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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class ProducerTest {

    @Test
    public void testProductions() throws ExecutionException, InterruptedException {
        List<TestProduction> productions = Arrays.asList(
                new TestProduction("a"),
                new TestProduction("b"),
                new TestProduction("c"),
                new TestProduction("d"),
                new TestProduction("e"),
                new TestProduction("f")

        );
        Producer producer = new RingBasedProducer(2);
        List<String> results = Lists.transform(producer.completeAll(productions), new Function<Future<String>, String>() {
            @Override
            public String apply(Future<String> future) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return null;
                } catch (ExecutionException e) {
                    return null;
                }
            }
        });
        assertThat(results.size(), is(6));
        assertThat(results, hasItems("a", "b", "c", "d", "e", "f"));
    }

    private static class TestProduction implements Production<String> {

        private final String value;

        public TestProduction(String value) {
            this.value = value;
        }

        @Override
        public Future<String> produce() {
            FutureTask<String> result = new FutureTask<String>(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200 + new Random().nextInt(400));
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }, value);
            new Thread(result).start();
            return result;
        }
    }

}
