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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BufferedProducerTest {

    @Mock
    private Future<String> testFuture;

    @Test
    public void shouldHandleAll() {
        List<TestProduction> productions = Arrays.asList(
                new TestProduction("a"),
                new TestProduction("b"),
                new TestProduction("c"),
                new TestProduction("d"),
                new TestProduction("e"),
                new TestProduction("f")
        );
        TaskExecutor producer = new BufferedTaskExecutor(2);
        List<String> results = new LinkedList<String>();
        producer.execute(productions, results);
        assertThat(results, hasItems("a", "b", "c", "d", "e", "f"));
    }

    @Test
    public void shouldHandleExecutionException() throws ExecutionException, InterruptedException {
        when(testFuture.get()).thenThrow(new ExecutionException(new IllegalStateException("Whoops, problems")));
        List<Task<String>> productions = Arrays.asList(
                new TestProduction("a"),
                new TestProduction("b"),
                new Task<String>() {
                    @Override
                    public Future<String> start() {
                        return testFuture;
                    }
                },
                new TestProduction("d"),
                new TestProduction("e"),
                new TestProduction("f")
        );
        TaskExecutor producer = new BufferedTaskExecutor(2);
        List<String> results = new LinkedList<String>();
        producer.execute(productions, results);
        assertThat(results, hasItems("a", "b", "d", "e", "f"));
    }

    public void shouldHandleInterruptedException() throws ExecutionException, InterruptedException {
        when(testFuture.get()).thenThrow(new InterruptedException());
        List<Task<String>> productions = Arrays.asList(
                new TestProduction("a"),
                new TestProduction("b"),
                new Task<String>() {
                    @Override
                    public Future<String> start() {
                        return testFuture;
                    }
                },
                new TestProduction("d"),
                new TestProduction("e"),
                new TestProduction("f")
        );
        TaskExecutor producer = new BufferedTaskExecutor(2);
        List<String> results = new LinkedList<String>();
        producer.execute(productions, results);
        assertThat(results, hasItems("a", "b", "d", "e", "f"));
    }

    private static class TestProduction implements Task<String> {

        private final String value;

        public TestProduction(String value) {
            this.value = value;
        }

        @Override
        public Future<String> start() {
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
