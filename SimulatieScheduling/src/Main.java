import Components.Schedulers.*;
import Components.Schedulers.MLFBs.MLFBWithAbsPriority;
import Components.Schedulers.MLFBs.MLFBWithTimeSlicing;
import Simulator.Simulator;
import jdk.jshell.spi.ExecutionControl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionControl.NotImplementedException {

        /*
        Add Simulations first. Select a Queue, input file and output file.
         */
        System.out.println("#### Intitialising simulations... ####");
        List<Simulator> simulators = List.of(
                new Simulator(new FCFS(), "processen50000.xml", "results/fcfs-50k.csv"),
                new Simulator(new SJF(),  "processen50000.xml", "results/sjf-50k.csv"),
                new Simulator(new SRT(),  "processen50000.xml", "results/srt-50k.csv"),
                new Simulator(new RR(2), "processen50000.xml", "results/rr2-50k.csv"),
                new Simulator(new RR(4), "processen50000.xml", "results/rr4-50k.csv"),
                new Simulator(new RR(8), "processen50000.xml", "results/rr8-50k.csv"),
                new Simulator(new HRRN(), "processen50000.xml", "results/hrrn-50k.csv"),
                                                                                                                        //4 RR levels, start at q=4->...->32, and 64 aging and abs priority
                new Simulator(new MLFBWithAbsPriority(new int[]{4, 8, 16, 32}, 64), "processen50000.xml", "results/mlfb-4.4-64-abs-50k.csv"),
                new Simulator(new MLFBWithTimeSlicing(new int[]{4, 8, 16, 32}, new int[]{40, 20, 10, 5, 2}, 64), "processen50000.xml", "results/mlfb-4-64-prop-50k.csv")
        );

        // Simulations are ran in parallel.
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        System.out.println("#### Running simulations... ####");
        long start = System.nanoTime();

        for (Simulator sim : simulators) {
            pool.submit(() -> {
                System.out.println("-- Starting " + sim.getUsedSchedulerTypeName() + " simulation --");
                sim.run();
                System.out.println("-- Finished " + sim.getUsedSchedulerTypeName() + " simulation --");
            });
        }

        pool.shutdown();
        //interrupt if simulations take longer then 1 minute (should never happen)
        pool.awaitTermination(1, TimeUnit.MINUTES);

        long end = System.nanoTime();
        System.out.println("###################################");
        System.out.printf("Simulations finished in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(end - start));
        System.out.println("###################################");

    }
}
