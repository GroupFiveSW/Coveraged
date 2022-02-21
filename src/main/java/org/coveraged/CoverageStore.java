package org.coveraged;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class CoverageStore {
    static HashMap<String, boolean[]> branchMap = new HashMap<>();
    public static String path = "store";

    public static void main(String[] args) {
        double cov = getTotalCoverage();
        System.out.println(cov);
    }

    public static <T> T wrap(T val, String methodId, int branchId) {
        branchMap.get(methodId)[branchId] = true;
        return val;
    }

    public static void init(String methodId, int count) {
        branchMap.putIfAbsent(methodId, new boolean[count]);
    }

    public static void writeToFile(String methodId) {
        File file = new File(path);
        PrintStream out = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new PrintStream(new FileOutputStream(file, true));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String methodName = methodId;
        boolean[] methodBranches = branchMap.get(methodName);
        out.println("init " + methodName + " " + methodBranches.length);
        for (int i = 0; i < methodBranches.length; i++) {
            if (methodBranches[i]) {
                out.println("wrap " + methodName + " " + i);
            }
        }

        out.close();
    }

    private static String resolveCaller() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        return caller.getClassName() + "::" + caller.getMethodName();
    }


    public static HashMap<String, boolean[]> result() {
        HashMap<String, boolean[]> res = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) {
            return res;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] contents = line.split(" ");
                if (contents[0].equals("init")) {
                    res.putIfAbsent(contents[1], new boolean[Integer.parseInt(contents[2])]);
                } else {
                    res.get(contents[1])[Integer.parseInt(contents[2])] = true;
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static double getTotalCoverage() {
        HashMap<String,boolean[]> branchMap = result();
        if (branchMap.isEmpty()) {
            System.out.println("No coverage detected");
            return 0.0;
        }

        double totalCoverage = 0;
        double branchCounter = 0;

        for (Map.Entry<String, boolean[]> method : branchMap.entrySet()) {
            boolean[] branches = method.getValue();
            System.out.println(method.getKey());
            double tfSum = 0;
            for (boolean tf : branches) {
                if (tf) {
                    branchCounter++;
                    tfSum++;
                }
            }
            System.out.print((int)tfSum + "/" + branches.length);
            System.out.println(" (" + String.format("%.2f", tfSum / branches.length * 100) + "%)");
            totalCoverage += method.getValue().length;
        }

        System.out.println("Total");
        System.out.println((int)branchCounter + " branches taken out of " + (int)totalCoverage);

        return (totalCoverage > 0.0) ? (branchCounter / totalCoverage) * 100 : 0;
    }
}