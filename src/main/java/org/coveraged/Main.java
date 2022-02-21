package org.coveraged;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;


import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static String projectPath;
    private static String projectJDK;
    private static String setupPath = "src/setup/";
    private static ArrayList<String> functionPaths = new ArrayList<>();
    private static HashMap<String, String> properties = new HashMap<>();

    public static void main(String[] args) throws Exception {
        initializeFunctionPaths();
        initializeProperties();
        projectPath = properties.get("algorithms.dir");
        projectJDK = properties.get("algorithms.java_home");
        System.out.println("Injecting code in functions...");

        var coverageStore = "src/main/java/org/coveraged/CoverageStore.java";
        var coverageStoreLocal = new File(coverageStore);
        var coverageStoreRemote = new File(projectPath + coverageStore);
        Files.copy(coverageStoreLocal.toPath(), coverageStoreRemote.toPath(), StandardCopyOption.REPLACE_EXISTING);
        CoverageStore.path = projectPath + "store";

        var originalFiles = new HashMap<String, String>();
        for (String functionPath : functionPaths) {
            String methodName = functionPath.split(" ")[1];
            System.out.println("Method name :" + methodName);
            String path = projectPath + functionPath.split(" ")[0];
            var cu = StaticJavaParser.parse(new File(path));

            originalFiles.put(path, cu.toString());

            cu.addImport("org.coveraged.CoverageStore");
            var meth = cu.findFirst(MethodDeclaration.class, (method) -> method.getName().asString().equals(methodName)).get();

            modifyMethod(meth, path);
            var writer = new FileWriter(path);
            writer.write(cu.toString());
            writer.close();
        }


        System.out.println("Running tests...");
        Runtime run = Runtime.getRuntime();

        Process process = run.exec(new String[]{
                        projectPath + "/gradlew.bat",
                        "-Dorg.gradle.java.home=\"" + projectJDK + "\"",
                        "test"},
                null, new File(projectPath));
        process.waitFor();
        System.out.println("Tests ran successfully");


        double coverage = CoverageStore.getTotalCoverage();

        System.out.println("Coverage is: " + coverage + "%.");

        try {
            Files.deleteIfExists(Path.of(projectPath + "store"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Restoring files");
        for (var origFile : originalFiles.entrySet()) {
            var writer = new FileWriter(origFile.getKey());
            writer.write(origFile.getValue());
            writer.close();
        }
    }

    private static void initializeFunctionPaths() {
        File file = new File(setupPath + "setupfull.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                functionPaths.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeProperties() {
        File file = new File(setupPath + "properties.txt");
        try {
            var br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                var split = line.split("=");
                properties.put(split[0], split[1]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Statement createWriteStmt(String methodId) {
        MethodCallExpr writeExpr = StaticJavaParser.parseExpression("CoverageStore.writeToFile(\"\")");
        writeExpr.setArgument(0, new StringLiteralExpr(methodId));
        return new ExpressionStmt(writeExpr);
    }

    private static void modifyMethod(MethodDeclaration method, String path) {
        String name = path+"::"+method.getNameAsString();
        var methodBody = method.getBody().get();

        // Add branch marks
        int offset = modifyIf(methodBody, name, 0);
        offset = modifyFor(methodBody, name, offset);
        offset = modifyWhile(methodBody, name, offset);
        offset = modifyDoWhile(methodBody, name, offset);
        offset = modifyTernary(methodBody, name, offset);
        var methodStmts = methodBody.getStatements();
        var lastStmt = methodStmts.getLast().get();
        if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
            methodStmts.addBefore(new ExpressionStmt(createWrapExpr(name, offset)), lastStmt);
        } else {
            methodStmts.addAfter(createWriteStmt(name), lastStmt);
            methodStmts.addAfter(new ExpressionStmt(createWrapExpr(name, offset)), lastStmt);
        }
        offset++;

        // Add file write before exit statements
        for (var stmt : methodBody.findAll(ReturnStmt.class)) {
            var blockStmt = ((BlockStmt) stmt.getParentNode().get());
            blockStmt.getStatements().addBefore(createWriteStmt(name), stmt);
        }
        for (var stmt : methodBody.findAll(ThrowStmt.class)) {
            var blockStmt = ((BlockStmt) stmt.getParentNode().get());
            blockStmt.getStatements().addBefore(createWriteStmt(name), stmt);
        }

        // Add init at beginning of method
        int count = offset;
        MethodCallExpr initMethod = StaticJavaParser.parseExpression("CoverageStore.init(\"\",0)");
        initMethod.setArgument(0, new StringLiteralExpr(name));
        initMethod.setArgument(1, intExpr(count));
        methodBody.addStatement(0, initMethod);

        // Declare method exists
        CoverageStore.init(name, count);
    }

    private static Statement asBlockStatement(Statement stmt) {
        if (stmt.isBlockStmt()) {
            return stmt;
        }
        return new BlockStmt().addStatement(stmt);
    }

    private static IntegerLiteralExpr intExpr(int val) {
        return new IntegerLiteralExpr(String.valueOf(val));
    }

    private static MethodCallExpr createWrapExpr(String methodId, int branchId) {
        return createWrapExpr(null, methodId, branchId);
    }

    private static MethodCallExpr createWrapExpr(Expression inner, String methodId, int branchId) {
        MethodCallExpr wrapExpr = StaticJavaParser.parseExpression("CoverageStore.wrap(null, \"\", 0)");
        if (inner != null) wrapExpr.setArgument(0, inner);
        wrapExpr.setArgument(1, new StringLiteralExpr(methodId));
        wrapExpr.setArgument(2, intExpr(branchId));
        return wrapExpr;
    }

    private static int modifyIf(BlockStmt methodBody, String methodId, int idOffset) {
        for (var stmt : methodBody.findAll(IfStmt.class)) {
            stmt.setThenStmt(asBlockStatement(stmt.getThenStmt()));
            stmt.getThenStmt().asBlockStmt().addStatement(0, createWrapExpr(methodId, idOffset));
            var stmts = stmt.getThenStmt().asBlockStmt().getStatements();
            var lastStmt = stmts.getLast().get();
            if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
                stmts.addBefore(createWriteStmt(methodId), lastStmt);
            }

            if (stmt.getElseStmt().isPresent() && !stmt.getElseStmt().get().isIfStmt()) {
                stmt.setElseStmt(asBlockStatement(stmt.getElseStmt().get()));
                stmt.getElseStmt().get().asBlockStmt().addStatement(0, createWrapExpr(methodId, idOffset));
            }
            idOffset++;
        }
        return idOffset;
    }

    private static int modifyFor(BlockStmt methodBody, String methodId, int idOffset) {
        for (var stmt : methodBody.findAll(ForStmt.class)) {
            stmt.setBody(asBlockStatement(stmt.getBody()));
            stmt.getBody().asBlockStmt().addStatement(0, createWrapExpr(methodId, idOffset));
            var stmts = stmt.getBody().asBlockStmt().getStatements();
            var lastStmt = stmts.getLast().get();
            if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
                stmts.addBefore(createWriteStmt(methodId), lastStmt);
            }
            idOffset++;
        }
        return idOffset;
    }

    private static int modifyWhile(BlockStmt methodBody, String methodId, int idOffset) {
        for (var stmt : methodBody.findAll(WhileStmt.class)) {
            stmt.setBody(asBlockStatement(stmt.getBody()));
            stmt.getBody().asBlockStmt().addStatement(0, createWrapExpr(methodId, idOffset));
            var stmts = stmt.getBody().asBlockStmt().getStatements();
            var lastStmt = stmts.getLast().get();
            if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
                stmts.addBefore(createWriteStmt(methodId), lastStmt);
            }
            idOffset++;
        }
        return idOffset;
    }

    private static int modifyDoWhile(BlockStmt methodBody, String methodId, int idOffset) {
        for (var stmt : methodBody.findAll(DoStmt.class)) {
            stmt.setBody(asBlockStatement(stmt.getBody()));
            stmt.getBody().asBlockStmt().addStatement(0, createWrapExpr(methodId, idOffset));
            var stmts = stmt.getBody().asBlockStmt().getStatements();
            var lastStmt = stmts.getLast().get();
            if (lastStmt.isReturnStmt() || lastStmt.isThrowStmt()) {
                stmts.addBefore(createWriteStmt(methodId), lastStmt);
            }
            idOffset++;
        }
        return idOffset;
    }

    private static int modifyTernary(BlockStmt methodBody, String methodId, int idOffset) {
        for (var expr : methodBody.findAll(ConditionalExpr.class)) {
            expr.setThenExpr(createWrapExpr(expr.getThenExpr(), methodId, idOffset));
            idOffset++;
            expr.setElseExpr(createWrapExpr(expr.getElseExpr(), methodId, idOffset));
            idOffset++;
        }
        return idOffset;
    }
}
