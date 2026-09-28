package Components.Schedulers.MLFBs;

import Components.Process;
import Components.Schedulers.BaseScheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MLFBWithAbsPriority extends BaseScheduler {

    private record ProcessEntry(Process process, long elapsedTime, long waitStart){
        public long getRemainingTime(){
            return process.getServiceTime() - elapsedTime;
        }
        public long lastWaitStart(){
            return waitStart;
        }
    }

    private final List<Queue<ProcessEntry>> queues;
    private ProcessEntry currentActive = null;
    private final int[] timeSlices;
    private final long agingThreshold;
    private long sliceStart;
    private int currentPriorityLevel;

    public MLFBWithAbsPriority(int[] timeSlices, long agingThreshold){
        this.timeSlices = timeSlices;
        this.agingThreshold = agingThreshold;
        // RR queues + 1 FCFS queue
        this.queues = new ArrayList<>(timeSlices.length + 1);
        for(int i = 0; i < timeSlices.length + 1; i++){
            queues.add(new LinkedList<>());
        }
    }

    @Override
    public void schedule(Process process) {
        queues.get(0).add(new ProcessEntry(process, 0, 0));
    }

    @Override
    public void update(long time) {
        promoteStarvingProcess(time);

        //Start new process
        if(currentActive == null){
            setNextProcess(time);
        }
        //process already running
        else{
            long elapsed = currentActive.elapsedTime() + (time - sliceStart);

            //process finished
            if(elapsed >= currentActive.process().getServiceTime()){
                finishProcess(time);
            }
            //higher priority waiting
            else if(isHigherPriorityProcessWaiting()){
                queues.get(currentPriorityLevel).add(new ProcessEntry(currentActive.process(), elapsed, time));
                setNextProcess(time);
            }
            //expired timeSlice => demote (unless at FCFS level)
            else if(!isCurrentInFCFS() && time-sliceStart >= timeSlices[currentPriorityLevel] ){
                queues.get(currentPriorityLevel + 1).add(new ProcessEntry(currentActive.process(), elapsed, time));
                setNextProcess(time);
            }
        }
    }

    @Override
    public boolean isIdle() {
        boolean allQueuesEmpty = true;
        for(Queue<ProcessEntry> q : queues){
            allQueuesEmpty &= q.isEmpty();
        }

        return allQueuesEmpty && currentActive == null;
    }

    private void setNextProcess(long time){

        for (int i = 0; i < queues.size(); i++) {
            Queue<ProcessEntry> q = queues.get(i);
            if (!q.isEmpty()) {
                currentActive = q.poll();
                if(currentActive != null){
                    if(currentActive.elapsedTime() == 0){
                        currentActive.process().setStartTime(time);
                    }
                    sliceStart = time;
                }
                currentPriorityLevel = i;
                return;
            }
        }
        currentActive = null;
        currentPriorityLevel = 0;
    }

    private void promoteStarvingProcess(long time){
        //promote starved processes on all levels (except the highest priority - lvl 0)
        for (int i = 1; i < queues.size(); i++) {
            Queue<ProcessEntry> q = queues.get(i);
            if (!q.isEmpty() && time - q.peek().lastWaitStart() >= agingThreshold) {
                ProcessEntry entry = q.poll();
                int promotedLevel = i - 1;
                queues.get(promotedLevel).add(new ProcessEntry(entry.process(), entry.elapsedTime() ,time));
            }
        }
    }

    private boolean isCurrentInFCFS(){
        return currentPriorityLevel == timeSlices.length;
    }

    private boolean isHigherPriorityProcessWaiting(){
        for(int i = 0; i < currentPriorityLevel; i++){
            if(!queues.get(i).isEmpty()) return true;
        }
        return false;
    }

    private void finishProcess(long time){
        currentActive.process().setEndTime(time);
        finishedProcesses.add(currentActive.process());
        currentActive = null;
    }
}
