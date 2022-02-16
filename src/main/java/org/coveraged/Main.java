package org.coveraged;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.*;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static MethodCallExpr wrapExpr = StaticJavaParser.parseExpression("CoverageStore.wrap(null, \"\", 0)");

    public static void main(String[] args) throws Exception {
        //if (args.length != 1) {
        //    throw new Exception("no");
        //}
        //var algorithmsPath = args[0];

        var testClass = new File("src/main/java/org/coveraged/TestClass.java");

        var cu = StaticJavaParser.parse(testClass);

        cu.findAll(ClassOrInterfaceDeclaration.class).stream().forEach((foundClass) -> {
            foundClass.findAll(MethodDeclaration.class).stream().forEach((method) -> {
                var path = foundClass.getFullyQualifiedName().get();
                modifyMethod(method, path);
            });
        });

        var writer = new FileWriter(testClass);
        writer.write(cu.toString());
        writer.close();
    }

    private static void modifyMethod(MethodDeclaration method, String path){
        String name = path+"::"+method.getNameAsString();
        AtomicInteger offset = modifyIf(method.getBody().get(), name, new AtomicInteger(0));
        offset = modifyFor(method.getBody().get(), name, offset);
        offset = modifyWhile(method.getBody().get(), name, offset);
        offset = modifyDoWhile(method.getBody().get(), name, offset);
        int count = modifyTernary(method.getBody().get(), name, offset).get();
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
            if (!thenBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(newWrapExpr)) {
                thenBlock.addStatement(0, newWrapExpr);
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
                idOffset.getAndIncrement();
                newWrapExpr = wrapExpr.clone()
                        .setArgument(1, new StringLiteralExpr(methodId))
                        .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));
                if (!elseBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(newWrapExpr)) {
                    elseBlock.addStatement(0, wrapExpr);
                    stmt.setElseStmt(elseBlock);
                }
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
            if (!bodyBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(newWrapExpr)) {
                bodyBlock.addStatement(0, newWrapExpr);
                stmt.setBody(bodyBlock);
            }
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
            if (!bodyBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(newWrapExpr)) {
                bodyBlock.addStatement(0, newWrapExpr);
                stmt.setBody(bodyBlock);
            }
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
            if (!bodyBlock.getStatements().getFirst().get().asExpressionStmt().getExpression().equals(newWrapExpr)) {
                bodyBlock.addStatement(0, newWrapExpr);
                stmt.setBody(bodyBlock);
            }
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
            if (!thenExpr.isMethodCallExpr()) {
                thenExpr = newWrapExpr.setArgument(0,thenExpr);
                expr.setThenExpr(thenExpr);
            } else if(!thenExpr.asMethodCallExpr().getName().equals(wrapExpr.getName())) {
                thenExpr = newWrapExpr.setArgument(0,thenExpr);
                expr.setThenExpr(thenExpr);
            }
            idOffset.getAndIncrement();
            newWrapExpr = wrapExpr.clone()
                    .setArgument(1, new StringLiteralExpr(methodId))
                    .setArgument(2, new IntegerLiteralExpr(String.valueOf(idOffset.get())));
            if (!elseExpr.isMethodCallExpr()) {
                elseExpr = newWrapExpr.setArgument(0, elseExpr);
                expr.setElseExpr(elseExpr);
            } else if (!elseExpr.asMethodCallExpr().getName().equals(wrapExpr.getName())) {
                elseExpr = newWrapExpr.setArgument(0, elseExpr);
                expr.setElseExpr(elseExpr);
            }
            idOffset.getAndIncrement();
        });
        return idOffset;
    }
}
