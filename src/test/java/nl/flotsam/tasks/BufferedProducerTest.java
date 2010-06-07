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
