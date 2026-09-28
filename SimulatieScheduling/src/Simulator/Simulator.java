package Simulator;

import java.io.FileNotFoundException;

import Components.Process;
import Components.Schedulers.*;
import FileManager.*;

/*
The simulator class holds all information to run a simulation and handles the simulation itself.
This way, multiple different simulations can be created.

Call run() to start a simulation.

The simulation is synchronised using a clock. This value gets passed around to determine updates.
 */
public class Simulator {

    private final Scheduler scheduler;
    private final String inputFile;
    private final String outputFile;

    public Simulator(Scheduler scheduler, String inputFile, String outputFile){
        this.scheduler = scheduler;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void run(){
        try(FileLoader inFile = new FileLoader(); FileWriter outFile = new FileWriter()){
            //open the respective files to read from and write to.
            inFile.loadFile(inputFile);
            outFile.openFile(outputFile, FileWriter.OverwriteMode.OVERWRITE);

            //Get the first "process"
            Components.Process process = inFile.getNextProcess();
            long clock = 0;
            //Run simulation as long as there are processes to queue
            //and the current scheduler is not buzy anymore (waiting/idle)
            while(process != null || !scheduler.isIdle()){
                //If there is a process to be scheduled, and its arrival time has come => schedule it.
                if(process != null && clock >= process.getArrivalTime()) {
                    scheduler.schedule(process);
                    process = inFile.getNextProcess();
                }

                scheduler.update(clock);
                clock++;

                //Write finished processes to file
                Process finished;
                while ((finished = scheduler.getFinishedProcess()) != null) {
                    outFile.writeToFile(finished.toCsvString());
                }
            }

        }
        catch (FileNotFoundException e){
            System.out.println("File error: " + e.getMessage());
        }
        catch (Exception e){
            System.out.println("Simulation failed: " + e.getMessage());
        }
    }

    public String getUsedSchedulerTypeName(){
        return scheduler.getClass().getSimpleName();
    }
    
}
