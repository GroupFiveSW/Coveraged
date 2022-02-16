package org.coveraged;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.io.File;
import java.io.FileWriter;

public class Main {
    private static MethodCallExpr myStmt = StaticJavaParser.parseExpression("CoverageRecorder.wrap(null, \"\")");

    public static void main(String[] args) throws Exception {
        //if (args.length != 1) {
        //    throw new Exception("no");
        //}
        //var algorithmsPath = args[0];

        var testClass = new File("src/main/java/org/coveraged/TestClass.java");

        var cu = StaticJavaParser.parse(testClass);

        modifyIf(cu);
        modifyFor(cu);
        modifyWhile(cu);
        modifyDoWhile(cu);
        modifyTernary(cu);

        System.out.println(cu.toString());

        var writer = new FileWriter(testClass);
        writer.write(cu.toString());
        writer.close();
    }

    private static void modifyIf(CompilationUnit cu) {
        cu.findAll(IfStmt.class).stream().forEach((stmt) -> {
            // Set then statement
            var thenStmt = stmt.getThenStmt();
            if (!thenStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(thenStmt);
                stmt.setThenStmt(block);
            }
            var thenBlock = stmt.getThenStmt().asBlockStmt();
            if (!thenBlock.getStatements().getFirst().get().equals(myStmt)) {
                var newStmt = myStmt.clone().setArgument(1, new StringLiteralExpr("if block"));
                thenBlock.addStatement(0, newStmt);
                stmt.setThenStmt(thenBlock);
            }

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
                if (!elseBlock.getStatements().getFirst().get().equals(myStmt)) {
                    elseBlock.addStatement(0, myStmt);
                    stmt.setElseStmt(elseBlock);
                }
            }
        });
    }

    private static void modifyFor(CompilationUnit cu){
        cu.findAll(ForStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            if (!bodyBlock.getStatements().getFirst().get().equals(myStmt)) {
                bodyBlock.addStatement(0, myStmt);
                stmt.setBody(bodyBlock);
            }
        });
    }

    private static void modifyWhile(CompilationUnit cu){
        cu.findAll(WhileStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            if (!bodyBlock.getStatements().getFirst().get().equals(myStmt)) {
                bodyBlock.addStatement(0, myStmt);
                stmt.setBody(bodyBlock);
            }
        });
    }

    private static void modifyDoWhile(CompilationUnit cu){
        cu.findAll(DoStmt.class).stream().forEach((stmt) -> {
            var bodyStmt = stmt.getBody();
            if (!bodyStmt.isBlockStmt()) {
                var block = StaticJavaParser.parseStatement("{}");
                block.asBlockStmt().addStatement(bodyStmt);
                stmt.setBody(block);
            }
            var bodyBlock = stmt.getBody().asBlockStmt();
            if (!bodyBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(myStmt)) {
                bodyBlock.addStatement(0, myStmt);
                stmt.setBody(bodyBlock);
            }
        });
    }

    private static void modifyTernary(CompilationUnit cu){
        cu.findAll(ConditionalExpr.class).stream().forEach((expr) -> {
            System.out.println(expr);
        });
    }
}
