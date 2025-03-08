package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.reporting.RuleContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author JustinLiu
 */
public class ViolationUtils {
    public static void addViolationWithPrecisePosition(Rule rule, Node node, Object data) {
        addViolationWithPrecisePosition(rule, node, data, null);
    }

    public static void addViolationWithPrecisePosition(Rule rule, Node node, Object data, String message) {
        // 不能直接使用rule.asCtx(data)，因为是protected方法
        // 而是应该从data中获取RuleContext
        RuleContext ruleContext = (RuleContext) data;

        if (node instanceof ASTFieldDeclaration) {
            ASTFieldDeclaration fieldDecl = (ASTFieldDeclaration) node;
            // 使用PMD 7.0兼容的方式获取子节点
            Node variableId = null;
            for (int i = 0; i < fieldDecl.getNumChildren(); i++) {
                Node child = fieldDecl.getChild(i);
                if (child.getClass().getSimpleName().equals("ASTVariableDeclaratorId")) {
                    variableId = child;
                    break;
                }
            }

            if (Objects.nonNull(variableId)) {
                addViolation(ruleContext, variableId, message);
                return;
            }
        }

        if (node instanceof ASTMethodDeclaration) {
            ASTMethodDeclaration methodDecl = (ASTMethodDeclaration) node;
            // 使用PMD 7.0兼容的方式获取方法名
            Node methodName = null;
            try {
                // 反射调用getName方法 - 如果存在的话
                // 尝试获取这个违规的方法名
                methodName = (Node) methodDecl.getClass().getMethod("getName").invoke(methodDecl);
            } catch (Exception e) {
                // 降级: 寻找可能包含名称的子节点
                for (int i = 0; i < methodDecl.getNumChildren(); i++) {
                    if (methodDecl.getChild(i).getClass().getSimpleName().contains("Name")) {
                        methodName = methodDecl.getChild(i);
                        break;
                    }
                }
            }

            if (Objects.nonNull(methodName)) {
                addViolation(ruleContext, methodName, message);
                return;
            }
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
    private static void addViolation(RuleContext ruleContext, Node node, String message) {
        if (StringUtils.isBlank(message)) {
            ruleContext.addViolation(node);
        } else {
            ruleContext.addViolationWithMessage(node, message);
        }
    }
}