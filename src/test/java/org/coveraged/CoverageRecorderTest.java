package org.coveraged;

import org.junit.jupiter.api.Test;


import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CoverageRecorderTest {

    double coverage;
    float totalCoverage = 1;
    long idCounter = 1;

    HashMap<Long, Boolean> branchMap = new HashMap<>();


    @Test
    void getTotalCoverageTest() {

        totalCoverage = 3;
        //1/3rd of branches was visited
        branchMap.put(idCounter, true);
        idCounter++;
        branchMap.put(idCounter, false);
        idCounter++;
        branchMap.put(idCounter, true);
        idCounter++;

        coverage = CoverageRecorder.getTotalCoverage(totalCoverage, branchMap);
        // 1/3rd = 66.6.
        assertEquals(66.66667175292969, coverage);
        assertEquals(4, idCounter);

    }


    @Test
    void getTotalCoverageNoBranchesVisistedTest() {

        totalCoverage = 5;
        //1/3rd of branches was visited
        branchMap.put(idCounter, false);
        idCounter++;
        branchMap.put(idCounter, false);
        idCounter++;
        branchMap.put(idCounter, false);
        idCounter++;

        coverage = CoverageRecorder.getTotalCoverage(totalCoverage, branchMap);
        // 1/3rd = 66.6.
        assertEquals(0, coverage);
        assertEquals(4, idCounter);

    }


    @Test
    void testTestClass() {


    }
}