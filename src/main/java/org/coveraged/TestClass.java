package org.coveraged;

public class TestClass {

    public void testMethod() {
        int f = 2;
        if (f == 3) {
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "");
            System.out.print("hej");
            f = 2;
            if (f == 1) {
                CoverageRecorder.wrap(null, "if block");
                CoverageRecorder.wrap(null, "if block");
                CoverageRecorder.wrap(null, "if block");
                CoverageRecorder.wrap(null, "if block");
                CoverageRecorder.wrap(null, "if block");
                CoverageRecorder.wrap(null, "");
                System.out.print("hej");
                f = 1;
            }
        } else if (f > 6) {
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "if block");
            CoverageRecorder.wrap(null, "");
            System.out.print("hej");
            f = 3;
        }
        var hello = true ? 123 : 321;
        for (int i = 0; i < 5; i++) {
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            System.out.print("hej");
            i++;
        }
        while (f == 2) {
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            System.out.print("hej");
            f = 3;
        }
        do {
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            CoverageRecorder.wrap(null, "");
            System.out.print("hej");
            f = 1;
        } while (f == 2);
        f = f == 0 ? 2 : 1;
        var g = true || false;
    }
}
