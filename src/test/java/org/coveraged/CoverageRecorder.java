package org.coveraged;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;


import java.awt.event.TextListener;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;


public class CoverageRecorder {
    static HashMap<String, boolean[]> branchMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

        //@TODO: Execute runner from main that injects code. Output is codebase with injected hashmap modifications

        //@TODO: To be set by other function
        // Total number of branches that was found in the code

        Main m = new Main();
        String[] functionPaths = new String[5];
        functionPaths[0] = "";
        functionPaths[1] = "";
        functionPaths[2] = "";
        functionPaths[3] = "";
        functionPaths[4] = "";

        double coverage;

        JUnitCore junit = new JUnitCore();
        junit.run(GenericAlgorithmTest.class);



        //@TODO: RUN INJECTED CODE


        //@TODO: If branch was detected, run next two lines


        coverage = CoverageStore.getTotalCoverage();
        System.out.println("Coverage is: " + coverage + "%.");

    }

    public static <T> T wrap(T val, String where, int branchId) {
        //System.out.println(where);
        branchMap.get(where)[branchId] = true;
        return val;
    }

    public static void init(String methodId, int count) {
        branchMap.putIfAbsent(methodId, new boolean[count]);
    }

    public static double getTotalCoverage(float totalCoverage, HashMap<Long, Boolean> branchMap){

        //Atomic long due to lambda functions needing a final value in Java
        AtomicLong branchCounter = new AtomicLong(0);

        // Count all instances where branches have been taken
        branchMap.forEach( (k,v) -> {
            if(v) branchCounter.getAndIncrement();
        } );

        // Return as percentage of total branches that exists
        return 100 * (branchCounter.get() / totalCoverage);
    }
}
