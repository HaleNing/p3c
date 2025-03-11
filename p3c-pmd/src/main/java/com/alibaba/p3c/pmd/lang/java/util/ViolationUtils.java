package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.reporting.RuleContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @author JustinLiu
 */
public class ViolationUtils {
    public static void addViolationWithPrecisePosition(Rule rule, JavaNode node, Object data) {
        addViolationWithPrecisePosition(rule, node, data, null);
    }

    /**
     * addViolationWithPrecisePosition
     * @param rule
     * @param node
     * @param data
     * @param message
     */
    public static void addViolationWithPrecisePosition(Rule rule, JavaNode node, Object data, String message) {

        /*
            ASTCompilationUnit (代表 .java 文件)
                └── ASTClassDeclaration (代表 class MyClass)
                  ├── ASTFieldDeclaration (代表 private int count;)
                  └── ASTMethodDeclaration (代表 public void increment() { ... })
                      ├── ASTModifiers (代表 public void)
                      │   └── ASTPublic
                      │   └── ASTVoid
                      ├── ASTName (代表 increment)
                      ├── ASTFormalParameters (代表 ())
                      │   └── ... (参数列表为空)
                      └── ASTBlock (代表 { count++; })
                          └── ... (方法体语句)
                          */

        // 不能直接使用rule.asCtx(data)，因为是protected方法
        // 而是应该从data中获取RuleContext
        RuleContext ruleContext = (RuleContext) data;

        if (node instanceof ASTFieldDeclaration) {
            ASTFieldDeclaration fieldDecl = (ASTFieldDeclaration) node;
            ASTVariableDeclarator astVariableDeclarator = fieldDecl.descendants(ASTVariableDeclarator.class).first();
            if (astVariableDeclarator != null) {
                ASTVariableId varId = astVariableDeclarator.getVarId();
                addViolation(ruleContext, varId, message);
                return;
            }
        }

        if (node instanceof ASTMethodDeclaration) {
            ASTMethodDeclaration methodDeclNode = (ASTMethodDeclaration) node;
            addViolation(ruleContext, methodDeclNode, message);
            return;
        }
        addViolation(ruleContext, node, message);
    }


    /**
     * report violation  报告违规
     *
     * @param ruleContext 报告的能力类
     * @param node        违规的节点位置
     * @param message     违规的信息
     */
    private static void addViolation(RuleContext ruleContext, JavaNode node, String message) {
        if (StringUtils.isBlank(message)) {
            ruleContext.addViolation(node);
        } else {
            ruleContext.addViolationWithMessage(node, message);
        }
    }
}