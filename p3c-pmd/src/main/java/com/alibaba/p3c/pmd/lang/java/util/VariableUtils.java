package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariableUtils {
    /**
     * 获取变量声明中的变量名
     *
     * @param node 变量声明节点
     * @return 变量名，如果有多个变量则返回第一个，如果无法获取则返回null
     */
    public static String getVariableNameByJavaNode(JavaNode node) {
        if (node == null) {
            return null;
        }
        ASTVariableDeclarator variableDeclarator = node.descendants(ASTVariableDeclarator.class).first();
        // variableDeclarator.getName() in PMD7 = variableDeclarator.getVarId().getName() in PMD7 = ASTVariableDeclaratorId.getName in PMD6
        return variableDeclarator != null ? variableDeclarator.getName() : null;
    }

    /**
     * 获取特定类型字段声明中的变量名
     *
     * @param fieldDeclaration 字段声明节点
     * @return 字段名，如果有多个变量则返回第一个，如果无法获取则返回null
     */
    public static String getVariableNameByASTFieldDeclaration(ASTFieldDeclaration fieldDeclaration) {
        return getVariableNameByJavaNode(fieldDeclaration);
    }

    /**
     * 获取本地变量声明中的变量名
     *
     * @param localVarDecl 本地变量声明节点
     * @return 变量名，如果有多个变量则返回第一个，如果无法获取则返回null
     */
    public static String getVariableNameByASTLocalVariableDeclaration(ASTLocalVariableDeclaration localVarDecl) {
        return getVariableNameByJavaNode(localVarDecl);
    }

    /**
     * 获取声明节点中的所有变量名
     *
     * @param node 变量声明节点
     * @return 变量名列表，如果无法获取则返回空列表
     */
    public static List<String> getAllVariableNames(JavaNode node) {
        if (node == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        node.descendants(ASTVariableDeclarator.class).forEach(declarator -> names.add(declarator.getName()));

        return names;
    }

    /**
     * 获取字段声明中的所有变量名
     *
     * @param fieldDeclaration 字段声明节点
     * @return 字段名列表，如果无法获取则返回空列表
     */
    public static List<String> getAllVariableNames(ASTFieldDeclaration fieldDeclaration) {
        return getAllVariableNames((JavaNode) fieldDeclaration);
    }

    /**
     * 获取本地变量声明中的所有变量名
     *
     * @param localVarDecl 本地变量声明节点
     * @return 变量名列表，如果无法获取则返回空列表
     */
    public static List<String> getAllVariableNames(ASTLocalVariableDeclaration localVarDecl) {
        return getAllVariableNames((JavaNode) localVarDecl);
    }
}