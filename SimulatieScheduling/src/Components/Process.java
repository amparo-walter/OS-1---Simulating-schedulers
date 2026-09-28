package Components;

/*
Object to store common data amongst all processes.
Has toString methods that return the process in a certain format.
 */
public class Process {
    private final long pid;
    private final long arrivalTime;
    private final long serviceTime;

    private long startTime = 0;
    private long endTime = 0;

    public Process(long pid, long arrivalTime, long serviceTime){
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
    }

    public long getPid(){
        return pid;
    }
    public long getArrivalTime() {
        return arrivalTime;
    }
    public long getServiceTime() {
        return serviceTime;
    }

    public long getStartTime(){
        return  startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getEndTime(){
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "PID: " + this.pid + "\n" +
                "Arrival time: "+this.arrivalTime + "\n" +
                "Process time: "+this.serviceTime + "\n" +
                "Start time: "+this.startTime + "\n" +
                "End time: "+this.endTime;
    }

    public String toXmlString() {
        return "<process>\n" +
                "   <pid>"          + this.pid         + "</pid>\n" +
                "   <arrivalTime>"  + this.arrivalTime + "</arrivalTime>\n" +
                "   <serviceTime>"  + this.serviceTime + "</serviceTime>\n" +
                "   <startTime>"    + this.startTime   + "</startTime>\n" +
                "   <endTime>"      + this.endTime     + "</endTime>\n" +
                "</process>";
    }

    public String toCsvString(){
        return this.pid + "," +
                this.arrivalTime + "," +
                this.serviceTime + "," +
                this.startTime + "," +
                this.endTime;
    }
}
