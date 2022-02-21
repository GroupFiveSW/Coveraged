package org.coveraged;

public class GenericAlgorithm {

    public static void main(String[] args) {
        new GenericAlgorithm().testMethod();
        double res = CoverageStore.getTotalCoverage();
        System.out.println(res);
    }

    public void testMethod() {
        int f = 2;
        if (f == 3) {
            f = 2;
            if (f == 1) {
                f = 1;
                return;
            }
        } else if (f > 6) {
            f = 3;
        }
        var hello = true ? 321 : 321;
        for (int i = 0; i < 5; i++) {
            i++;
        }
        while (f == 2) {
            f = 3;
        }
        do {
            f = 1;
        } while (f == 2);
        f = f == 0 ? 1 : 1;
        var g = true || false;
        return;
    }
}
