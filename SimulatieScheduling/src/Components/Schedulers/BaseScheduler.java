package Components.Schedulers;

import Components.Process;

import java.util.LinkedList;
import java.util.Queue;

/*
Base class for a scheduler.
All schedulers must have the Scheduler interface implemented AND must handle finished processes.

A finished process simply needs to be added to the "finishedProcesses" list.
 */
public abstract class BaseScheduler implements Scheduler{
    protected Queue<Process> finishedProcesses = new LinkedList<>();
    public final Process getFinishedProcess() {
        return finishedProcesses.poll();
    }
}
