package Components.Schedulers;

import Components.Process;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class SJF extends BaseScheduler{

    private final Queue<Process> processQueue = new PriorityQueue<>(
            Comparator.comparingLong(Process::getServiceTime)
    );
    private Process currentActive = null;

    @Override
    public void schedule(Process process) {
        processQueue.add(process);
    }

    @Override
    public void update(long time) {
        //SJF
        //The priority queue auto sorts based on service time
        //Therefor this code block is the same for FCFS
        if(currentActive == null){
            currentActive = processQueue.poll();
            //que not empty
            if(currentActive != null){
                currentActive.setStartTime(time);
            }
        }
        //finished process
        else if(currentActive.getStartTime() + currentActive.getServiceTime() <= time){
            currentActive.setEndTime(time);
            finishedProcesses.add(currentActive);
            currentActive = null;
        }
    }

    @Override
    public boolean isIdle() {
        return currentActive == null && processQueue.isEmpty();
    }
}
