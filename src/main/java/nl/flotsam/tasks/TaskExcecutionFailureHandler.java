package nl.flotsam.tasks;

import java.util.concurrent.ExecutionException;

/**
 * The object keeping track of exceptions thrown while executing a task.
 */
public interface TaskExcecutionFailureHandler {

    /**
     * Signals the occurrence of a InterruptedException.
     *
     * @return A boolean indicating if the {@link nl.flotsam.tasks.TaskExecutor} should continue executing tasks.
     */
    boolean onException(InterruptedException e);
    
    /**
     * Signals the occurrence of a ExecutionException.
     *
     * @return A boolean indicating if the {@link nl.flotsam.tasks.TaskExecutor} should continue executing tasks.
     */
    boolean onException(ExecutionException e);

}
