package Components.Schedulers;

import Components.Process;

/*
This is an interface to loosly couple different schedulers in code.
This helps in reducing complexity and aids adjusting the codebase for future projects or expantions.
 */

public interface Scheduler {
    public void schedule(Process process);
    public void update(long time);
    public Process getFinishedProcess();
    public boolean isIdle();
}
