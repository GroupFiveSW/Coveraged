package org.coveraged;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CoverageRecorder {
    public static void main(String[] args) throws Exception {

        //@TODO: Execute runner from main that injects code. Output is codebase with injected hashmap modifications

        //@TODO: To be set by other function
        // Total number of branches that was found in the code
        float totalCoverage = 3;

        double coverage;

        HashMap<Long, Boolean> branchMap = new HashMap<>();
        long idCounter = 1;

        //@TODO: RUN INJECTED CODE


        //@TODO: If branch was detected, run next two lines
        branchMap.put(idCounter, true);
        idCounter++;
        branchMap.put(idCounter, true);
        idCounter++;
        branchMap.put(idCounter, true);
        idCounter++;


        coverage = getTotalCoverage(totalCoverage, branchMap);
        System.out.println("Coverage is: " + coverage + "%.");

    }

    public static <T> T wrap(T val, String where, int branchId) {
        System.out.println(where);
        return val;
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
