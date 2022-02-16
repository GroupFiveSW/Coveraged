package org.coveraged;

import javax.imageio.IIOException;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;


public class CoverageStore {
    static HashMap<String, boolean[]> branchMap = new HashMap<>();
    static String path = "store";

    public static <T> T wrap(T val, String where, int branchId) {
        // wrap testMethod 3
        String content = "wrap " + where + " " + branchId;
        writeToFile(content);
        return val;
    }

    public static void init(String methodId, int count) {
        // init testMethod 8 -> file
        String content = "init " + methodId + " " + count;
        writeToFile(content);
        //branchMap.putIfAbsent(methodId, new boolean[count]);
    }



    public static void writeToFile(String text) {
        File file = new File(path);
        BufferedWriter bf = null;
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            bf = new BufferedWriter( new FileWriter(file, true));
            bf.write(text);
            bf.newLine();
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, boolean[]> result() {
        HashMap<String, boolean[]> res = new HashMap<>();
        File file = new File(path);
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

        double totalCoverage = 0;
        double branchCounter = 0;

        for (var method : branchMap.entrySet()) {
            for (boolean tf : method.getValue()) {
                if (tf) branchCounter++;
            }
            totalCoverage += method.getValue().length;
        }

         return (totalCoverage > 0.0) ? (branchCounter/totalCoverage)*100 : 0;
    }
}