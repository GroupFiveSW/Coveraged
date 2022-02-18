package org.coveraged;

import javax.imageio.IIOException;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;


public class CoverageStore {
    static HashMap<String, boolean[]> branchMap = new HashMap<>();
    static String path = "C:/Users/elias/Documents/KTH/SoftwareEngineering/Coveraged/store";

    public static void main(String[] args) {
        double cov = getTotalCoverage();
        System.out.println(cov);
    }
    public static <T> T wrap(T val, String where, int branchId) {
        // wrap testMethod 3
        branchMap.get(where)[branchId] = true;
        return val;
    }

    public static void init(String methodId, int count) {
        // init testMethod 8 -> file
//        String content = "init " + methodId + " " + count;
//        writeToFile(content);
        branchMap.putIfAbsent(methodId, new boolean[count]);
    }

    public static void writeToFile() {
        File file = new File(path);
        BufferedWriter bf = null;
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            bf = new BufferedWriter( new FileWriter(file, true));
            for (var method : branchMap.entrySet()) {
                var methodValue = method.getValue();
                String content = "init " + method.getKey() + " " + methodValue.length;
                bf.write(content);
                bf.newLine();
                for (int i = 0; i < method.getValue().length; i++) {
                    if (methodValue[i]) {
                        String wrap = "wrap " + method.getKey() + " " + i;
                        bf.write(wrap);
                        bf.newLine();
                    }
                }
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static HashMap<String, boolean[]> result() {
        HashMap<String, boolean[]> res = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) {
            return res;
        }
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
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

    public static double getTotalCoverage(){
        HashMap<String, boolean[]> branchMap;

        branchMap = result();
        if (branchMap.isEmpty()) {
            System.out.println("No coverage detected");
            return 0.0;
        }

        double totalCoverage = 0;
        double branchCounter = 0;

        for (var method : branchMap.entrySet()) {
            for (boolean tf : method.getValue()) {
                if (tf) branchCounter++;
            }
            totalCoverage += method.getValue().length;
        }

        System.out.println(branchCounter + " branches taken out of " + totalCoverage);

        return (totalCoverage > 0.0) ? (branchCounter/totalCoverage)*100 : 0;
    }
}