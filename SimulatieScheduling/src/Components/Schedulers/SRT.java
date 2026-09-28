package Components.Schedulers;

import Components.Process;

import java.util.Comparator;
import java.util.PriorityQueue;

public class SRT extends BaseScheduler{

    //Wrapper to hold elapsedTime for process
    private record ProcessEntry(Process process, long elapsedTime){
        public long getRemainingServiceTime(){
            return process.getServiceTime() - elapsedTime;
        }
    }

    private final PriorityQueue<ProcessEntry> processQueue = new PriorityQueue<>(
            Comparator.comparing(ProcessEntry::getRemainingServiceTime)
    );
    private ProcessEntry currentActive = null;
    private long currentScheduledStartTime;

    @Override
    public void schedule(Process process) {
        processQueue.add(new ProcessEntry(process, 0));
    }

    @Override
    public void update(long time) {
        //no running process
        if(currentActive == null){
            setProcess(time);
        }
        else {
            long elapsed = currentActive.elapsedTime() + (time - currentScheduledStartTime);

            // Process finished
            if (elapsed >= currentActive.process().getServiceTime()) {
                finishProcess(time);
            }
            // Check if a shorter process is waiting
            else if (!processQueue.isEmpty()) {
                ProcessEntry updated = new ProcessEntry(currentActive.process(), elapsed);

                if (processQueue.peek().getRemainingServiceTime() < updated.getRemainingServiceTime()) {
                    processQueue.add(updated);
                    setProcess(time);
                }
            }
        }
    }

    @Override
    public boolean isIdle() {
        return currentActive == null && processQueue.isEmpty();
    }

    private void setProcess(long time){
        currentActive = processQueue.poll();
        if(currentActive != null){
            if (currentActive.elapsedTime() == 0) {
                currentActive.process().setStartTime(time);
            }
            currentScheduledStartTime = time;
        }
    }

    private void finishProcess(long time){
        currentActive.process().setEndTime(time);
        finishedProcesses.add(currentActive.process());
        currentActive = null;
    }
}
