package Components.Schedulers;

import Components.Process;

import java.util.LinkedList;
import java.util.Queue;

public class RR extends BaseScheduler{

    //wrapper to hold elapsed time
    private record ProcessEntry(Process process, long elapsedTime){}

    private final Queue<ProcessEntry> processQueue = new LinkedList<>();
    private ProcessEntry currentActive = null;

    private final long timeSlice;
    private long sliceStart;

    public RR(int timeSlice){
        this.timeSlice = timeSlice;
    }

    @Override
    public void schedule(Process process) {
        processQueue.add(new ProcessEntry(process, 0));
    }

    @Override
    public void update(long time) {
        if (currentActive == null) {
            currentActive = processQueue.poll();
            if (currentActive != null) {
                if (currentActive.elapsedTime() == 0) {
                    currentActive.process().setStartTime(time);
                }
                sliceStart = time;
            }
        } else {
            long elapsed = currentActive.elapsedTime() + (time - sliceStart);

            // Process finished
            if (elapsed >= currentActive.process().getServiceTime()) {
                currentActive.process().setEndTime(time);
                finishedProcesses.add(currentActive.process());
                currentActive = null;

            }
            // Time slice expired
            else if (time - sliceStart >= timeSlice) {
                //add back to queue
                processQueue.add(new ProcessEntry(currentActive.process(), elapsed));
                currentActive = null;
            }
        }
    }

    @Override
    public boolean isIdle() {
        return currentActive == null && processQueue.isEmpty();
    }
}
