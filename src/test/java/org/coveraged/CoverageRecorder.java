package org.coveraged;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static MethodCallExpr wrapExpr = StaticJavaParser.parseExpression("CoverageStore.wrap(null, \"\", 0)");
    private static MethodCallExpr wrapToFileExpr = StaticJavaParser.parseExpression("CoverageStore.writeToFile()");
    private static String projectPath = "C:/Users/elias/Documents/KTH/SoftwareEngineering/Algorithms";
    private static String setupPath = "src/setup/setup.txt";
    private static ArrayList<String> functionPaths = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        initializeFunctionPaths();
        System.out.println("Injecting code in functions...");
        for (String functionPath: functionPaths) {
            String methodName = functionPath.split(" ")[1];
            System.out.println("Method name :" + methodName);
            String path = functionPath.split(" ")[0];
            var cu = StaticJavaParser.parse(new File(path));

            cu.addImport("com.williamfiset.algorithms.TestCoverage.CoverageStore");
            var meth = cu.findFirst(MethodDeclaration.class, (method) -> method.getName().asString().equals(methodName)).get();

            modifyMethod(meth, path);
            var writer = new FileWriter(new File(path));
            writer.write(cu.toString());
            writer.close();
        }

        Runtime run = Runtime.getRuntime();
        System.out.println("Running tests...");
        Process process = run.exec(projectPath+"/gradlew.bat -Dorg.gradle.java.home=\"C:\\Program Files\\Java\\jdk1.8.0_251\" test", null, new File(projectPath));
        process.waitFor();
        System.out.println("Tests ran successfully");


        double coverage = CoverageStore.getTotalCoverage();

        System.out.println("Coverage is: " + coverage + "%.");
        try{
            Files.deleteIfExists(Path.of("C:/Users/elias/Documents/KTH/SoftwareEngineering/Coveraged/store"));
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static void initializeFunctionPaths() {
        File file = new File(setupPath);
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                functionPaths.add(projectPath + "/" + line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void modifyMethod(MethodDeclaration method, String path){
        String name = path+"::"+method.getNameAsString();
        AtomicInteger offset = modifyIf(method.getBody().get(), name, new AtomicInteger(0));
        offset = modifyFor(method.getBody().get(), name, offset);
        offset = modifyWhile(method.getBody().get(), name, offset);
        offset = modifyDoWhile(method.getBody().get(), name, offset);
        offset = modifyTernary(method.getBody().get(), name, offset);
        var methodBody = method.getBody().get().getStatements();
        var lastStmt = methodBody.getLast().get();
        var newWrapExpr = wrapExpr.clone()
                .setArgument(1, new StringLiteralExpr(name))
                .setArgument(2, new IntegerLiteralExpr(String.valueOf(offset.getAndIncrement())));
        int length = methodBody.toArray().length;
        if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
            int index = length == 0 ? 0 : length-1;
            methodBody.add(index, new ExpressionStmt(newWrapExpr));
        } else {
            int index = length == 0 ? 0 : length;
            methodBody.add(index, new ExpressionStmt(newWrapExpr));
            methodBody.addLast(new ExpressionStmt(wrapToFileExpr));
        }
        int count = offset.get();
        MethodCallExpr initMethod = StaticJavaParser.parseExpression("CoverageStore.init(\"\", 0)");
        initMethod.setArgument(0, new StringLiteralExpr(name))
                .setArgument(1, new IntegerLiteralExpr(String.valueOf(count)));
        method.getBody().get().addStatement(0, initMethod);


    }

    private static AtomicInteger modifyIf(BlockStmt methodBody, String methodId, AtomicInteger idOffset) {
        methodBody.findAll(IfStmt.class).stream().forEach((stmt) -> {
            // Set then statement
            var thenStmt = stmt.getThenStmt();
            if (!thenStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(thenStmt);
                stmt.setThenStmt(block);
            }
            var thenBlock = stmt.getThenStmt().asBlockStmt();
            var newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            thenBlock.addStatement(0, newWrapExpr);
            stmt.setThenStmt(thenBlock);


            // Set else statement

            var elseStmtOpt = stmt.getElseStmt();
            if (elseStmtOpt.isPresent() && !elseStmtOpt.get().isIfStmt()){
                var elseStmt = elseStmtOpt.get();
                if (!elseStmt.isBlockStmt()){
                    var block = new BlockStmt();
                    block.addStatement(elseStmt);
                    stmt.setElseStmt(block);
                }
                var elseBlock = stmt.getElseStmt().get().asBlockStmt();
                idOffset.getAndIncrement();
                newWrapExpr = wrapExpr.clone()
                        .setArgument(1, new StringLiteralExpr(methodId))
                        .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

                elseBlock.addStatement(0, wrapExpr);
                stmt.setElseStmt(elseBlock);

            }
            idOffset.getAndIncrement();
        });
        return idOffset;
    }

    private static AtomicInteger modifyFor(BlockStmt methodBody,String methodId, AtomicInteger idOffset){
        methodBody.findAll(ForStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            var newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            bodyBlock.addStatement(0, newWrapExpr);
            stmt.setBody(bodyBlock);

            idOffset.getAndIncrement();
        });
        return idOffset;
    }

    private static AtomicInteger modifyWhile(BlockStmt methodBody,String methodId, AtomicInteger idOffset){
        methodBody.findAll(WhileStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            var newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            bodyBlock.addStatement(0, newWrapExpr);
            stmt.setBody(bodyBlock);

            idOffset.getAndIncrement();
        });
        return idOffset;
    }

    private static AtomicInteger modifyDoWhile(BlockStmt methodBody,String methodId, AtomicInteger idOffset){
        methodBody.findAll(DoStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            var newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            bodyBlock.addStatement(0, newWrapExpr);
            stmt.setBody(bodyBlock);

            idOffset.getAndIncrement();
        });
        return idOffset;
    }

    private static AtomicInteger modifyTernary(BlockStmt methodBody, String methodId, AtomicInteger idOffset){
        methodBody.findAll(ConditionalExpr.class).stream().forEach((expr) -> {
            var thenExpr = expr.getThenExpr();
            var elseExpr = expr.getElseExpr();
            var newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            thenExpr = newWrapExpr.setArgument(0,thenExpr);
            expr.setThenExpr(thenExpr);

            idOffset.getAndIncrement();
            newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));

            elseExpr = newWrapExpr.setArgument(0, elseExpr);
            expr.setElseExpr(elseExpr);

            idOffset.getAndIncrement();
        });
        return idOffset;
    }
}
