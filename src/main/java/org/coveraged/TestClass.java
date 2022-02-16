package org.coveraged;

public class TestClass {

    public void testMethod() {
        CoverageRecorder.init("org.coveraged.TestClass::testMethod", 8);
        int f = 2;
        if (f == 3) {
            CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 0);
            f = 2;
            if (f == 1) {
                CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 1);
                f = 1;
            }
        } else if (f > 6) {
            CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 2);
            f = 3;
        }
        var hello = true ? CoverageRecorder.wrap(321, "org.coveraged.TestClass::testMethod", 6) : CoverageRecorder.wrap(321, "org.coveraged.TestClass::testMethod", 6);
        for (int i = 0; i < 5; i++) {
            CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 3);
            i++;
        }
        while (f == 2) {
            CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 4);
            f = 3;
        }
        do {
            CoverageRecorder.wrap(null, "org.coveraged.TestClass::testMethod", 5);
            f = 1;
        } while (f == 2);
        f = f == 0 ? CoverageRecorder.wrap(1, "org.coveraged.TestClass::testMethod", 7) : CoverageRecorder.wrap(1, "org.coveraged.TestClass::testMethod", 7);
        var g = true || false;
    }
}
