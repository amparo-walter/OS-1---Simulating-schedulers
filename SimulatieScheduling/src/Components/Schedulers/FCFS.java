package Components.Schedulers;

import Components.Process;

import java.util.LinkedList;
import java.util.Queue;

public class FCFS extends BaseScheduler {
    private final Queue<Process> processQueue = new LinkedList<>();
    private Process currentActive = null;

    @Override
    public void schedule(Process process) {
        processQueue.add(process);
    }

    @Override
    public void update(long time){
        //FCFS
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
