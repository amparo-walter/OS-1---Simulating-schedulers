package Components.Schedulers;

import java.util.*;

import Components.Process;

public class HRRN extends BaseScheduler {

    private final List<Process> queue = new LinkedList<>();
    private Process currentActive = null;

    @Override
    public void schedule(Process process) {
        queue.add(process);
    }

    @Override
    public void update(long time) {
        if (currentActive == null) {
            if (!queue.isEmpty()) {
                currentActive = Collections.min(queue, Comparator.comparingDouble(p -> -responseRatio(p, time)));
                queue.remove(currentActive);
                currentActive.setStartTime(time);
            }
        } else if (currentActive.getStartTime() + currentActive.getServiceTime() <= time) {
            currentActive.setEndTime(time);
            finishedProcesses.add(currentActive);
            currentActive = null;
        }
    }

    @Override
    public boolean isIdle(){
        return currentActive == null && queue.isEmpty();
    }

    private double responseRatio(Process process, long time) {
        long waitingTime = time - process.getArrivalTime();
        return (waitingTime + process.getServiceTime()) / (double) process.getServiceTime();
    }
}