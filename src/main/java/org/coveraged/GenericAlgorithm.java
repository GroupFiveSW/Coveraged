package org.coveraged;


public class GenericAlgorithm {

    public static void main(String[] args) {
        testMethod();
        double res = CoverageStore.getTotalCoverage();
        System.out.println(res);
    }

    static public void testMethod() {
        CoverageStore.init("org.coveraged.TestClass::testMethod", 8);
        int f = 2;
        if (f == 3) {
            CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 0);
            f = 2;
            if (f == 1) {
                CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 1);
                f = 1;
            }
        } else if (f > 6) {
            CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 2);
            f = 3;
        }
        var hello = true ? CoverageStore.wrap(321, "org.coveraged.TestClass::testMethod", 6) : CoverageStore.wrap(321, "org.coveraged.TestClass::testMethod", 6);
        for (int i = 0; i < 5; i++) {
            CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 3);
            i++;
        }
        while (f == 2) {
            CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 4);
            f = 3;
        }
        do {
            CoverageStore.wrap(null, "org.coveraged.TestClass::testMethod", 5);
            f = 1;
        } while (f == 2);
        f = f == 0 ? CoverageStore.wrap(1, "org.coveraged.TestClass::testMethod", 7) : CoverageStore.wrap(1, "org.coveraged.TestClass::testMethod", 7);
        var g = true || false;
    }
}
