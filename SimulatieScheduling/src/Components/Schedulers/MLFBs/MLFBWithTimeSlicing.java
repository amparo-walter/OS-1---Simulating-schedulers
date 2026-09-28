package Components.Schedulers.MLFBs;

import java.util.*;
import Components.Process;
import Components.Schedulers.BaseScheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MLFBWithTimeSlicing extends BaseScheduler {

    public record ProcessEntry(Process process, long elapsedTime, long waitStart) {
        public long getRemainingTime() {
            return process.getServiceTime() - elapsedTime;
        }
        public long lastWaitStart() {
            return waitStart;
        }
    }

    private final List<Queue<ProcessEntry>> queues;
    private ProcessEntry currentActive = null;
    private final int[] timeSlices;
    private final int[] queueBudgets;
    private int[] remainingBudgets;
    private final long agingThreshold;
    private long sliceStart;
    private int currentPriorityLevel;


    public MLFBWithTimeSlicing(int[] timeSlices, int[] queueBudgets, long agingThreshold) {
        this.timeSlices = timeSlices;
        this.queueBudgets = queueBudgets;
        this.remainingBudgets = queueBudgets.clone();
        this.agingThreshold = agingThreshold;

        // Queue 0: SJF/SRT
        // Queue 1-N: FIFO (RR)
        this.queues = new ArrayList<>(timeSlices.length + 1);
        queues.add(new PriorityQueue<>(Comparator.comparingLong(ProcessEntry::getRemainingTime))); // queue 0: SRT
        for (int i = 1; i < timeSlices.length + 1; i++) {
            queues.add(new LinkedList<>()); // queues 1..N: FIFO
        }
    }

    @Override
    public void schedule(Process process) {
        queues.get(0).add(new ProcessEntry(process, 0, 0));
    }

    @Override
    public void update(long time) {
        promoteStarvingProcess(time);

        if (currentActive == null) {
            setNextProcess(time);
        } else {
            long elapsed = currentActive.elapsedTime() + (time - sliceStart);

            if (elapsed >= currentActive.process().getServiceTime()) {
                finishProcess(time);
                setNextProcess(time);
            }
            else if (hasHigherBudgetedQueueWithProcess()) {
                    queues.get(currentPriorityLevel).add(new ProcessEntry(currentActive.process(), elapsed, time));
                    currentActive = null;
                    setNextProcess(time);
            }
            else if (!isCurrentInFCFS() && time - sliceStart >= timeSlices[currentPriorityLevel]) {
                queues.get(currentPriorityLevel + 1).add(new ProcessEntry(currentActive.process(), elapsed, time));
                currentActive = null;
                setNextProcess(time);
            }
        }
    }

    @Override
    public boolean isIdle() {
        for (Queue<ProcessEntry> q : queues) {
            if (!q.isEmpty()) return false;
        }
        return currentActive == null;
    }

    private void setNextProcess(long time) {
        for (int i = 0; i < queues.size(); i++) {
            if (remainingBudgets[i] > 0 && !queues.get(i).isEmpty()) {
                activateFrom(i, time);
                return;
            }
        }

        // All budgets exhausted — reset cycle and try again
        boolean anyProcessExists = queues.stream().anyMatch(q -> !q.isEmpty());
        if (anyProcessExists) {
            remainingBudgets = queueBudgets.clone();
            for (int i = 0; i < queues.size(); i++) {
                if (remainingBudgets[i] > 0 && !queues.get(i).isEmpty()) {
                    activateFrom(i, time);
                    return;
                }
            }
        }

        currentActive = null;
        currentPriorityLevel = 0;
    }


    private void activateFrom(int level, long time) {
        currentActive = queues.get(level).poll();
        currentPriorityLevel = level;
        sliceStart = time;

        if (currentActive.elapsedTime() == 0) {
            currentActive.process().setStartTime(time);
        }

        int quantum = isCurrentInFCFS() ? Integer.MAX_VALUE : timeSlices[currentPriorityLevel];
        remainingBudgets[level] = Math.max(0, remainingBudgets[level] - quantum);
    }

    private void promoteStarvingProcess(long time) {
        for (int i = 1; i < queues.size(); i++) {
            Queue<ProcessEntry> q = queues.get(i);
            if (!q.isEmpty() && time - q.peek().lastWaitStart() >= agingThreshold) {
                ProcessEntry entry = q.poll();
                queues.get(i - 1).add(new ProcessEntry(entry.process(), entry.elapsedTime(), time));
            }
        }
    }

    private boolean hasHigherBudgetedQueueWithProcess() {
        for (int i = 0; i < currentPriorityLevel; i++) {
            if (remainingBudgets[i] > 0 && !queues.get(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean isCurrentInFCFS() {
        return currentPriorityLevel == timeSlices.length;
    }

    private void finishProcess(long time) {
        currentActive.process().setEndTime(time);
        finishedProcesses.add(currentActive.process());
        currentActive = null;
    }
}
