package com.alibaba.p3c.pmd.lang.java.rule.concurrent;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;

/**
 * [Mandatory] SimpleDataFormat is unsafe, do not define it as a static variable.
 * If have to, lock or DateUtils class must be used.
 *
 * @author XiNing.Liu
 * @date 2025/03/28
 */
public class AvoidCallStaticSimpleDateFormatRule extends AbstractAliRule {
    private static final String FORMAT_METHOD_NAME = "format";

    @Override
    public Object visit(ASTMethodDeclaration methodDeclNode, Object data) {
        if (methodDeclNode.hasModifiers(JModifier.SYNCHRONIZED)) {
            // If the method is synchronized, no violation
            return super.visit(methodDeclNode, data);
        }
        checkStaticSimpleDateFormatUsage(methodDeclNode, data);
        return super.visit(methodDeclNode, data);
    }

    /**
     * Check for static SimpleDateFormat usage within a method
     */
    private void checkStaticSimpleDateFormatUsage(ASTMethodDeclaration methodDeclaration, Object data) {
        // Track synchronized and lock blocks
        Set<String> localSimpleDateFormatNames = collectLocalSimpleDateFormatVariables(methodDeclaration);

        // Check all primary expressions for static SimpleDateFormat calls
        methodDeclaration.descendants(ASTPrimaryExpression.class).forEach(primaryExpression -> {
            if (isStaticSimpleDateFormatCall(primaryExpression, localSimpleDateFormatNames)) {
                if (!isInSynchronizedContext(primaryExpression)) {
                    // Report violation if not in a synchronized context
                    ASTFieldAccess astFieldAccess = primaryExpression.firstChild(ASTFieldAccess.class);
                    if (astFieldAccess != null) {
                        String name = astFieldAccess.getName();
                        addViolationWithMessage(data, primaryExpression, getMessage(), new Object[]{name});
                    }
                }
            }
        });
    }

    /**
     * Collect all local SimpleDateFormat variable names to exclude them from checks
     */
    private Set<String> collectLocalSimpleDateFormatVariables(ASTMethodDeclaration methodDeclaration) {
        Set<String> localSimpleDateFormatNames = new HashSet<>();

        methodDeclaration.descendants(ASTVariableDeclarator.class).forEach(variableDeclarator -> {
            if (SimpleDateFormat.class.getName().equals(variableDeclarator.getName())) {
                ASTVariableId varId = variableDeclarator.firstChild(ASTVariableId.class);
                if (Objects.nonNull(varId)) {
                    localSimpleDateFormatNames.add(varId.getName());
                }
            }
        });

        return localSimpleDateFormatNames;
    }

    /**
     * Check if a node is within a synchronized context (synchronized block or lock)
     */
    private boolean isInSynchronizedContext(Node node) {
        // Check if in a synchronized block
        ASTSynchronizedStatement syncStmt = node.ancestors(ASTSynchronizedStatement.class).first();
        if (syncStmt != null) {
            return true;
        }

        // Check if in a lock block
        // For this simple implementation, we'll just check for lock() method calls
        // In a real implementation, you would need more sophisticated lock tracking
        ASTBlock block = node.ancestors(ASTBlock.class).first();
        if (block != null) {
            for (ASTStatement stmt : block.children(ASTStatement.class)) {
                if (isLockStatement(stmt) && stmt.getBeginLine() < node.getBeginLine()) {
                    // Found a lock statement before this node in the same block
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a statement is a lock statement
     */
    private boolean isLockStatement(ASTStatement statement) {
        ASTExpressionStatement stmtExpr = statement.firstChild(ASTExpressionStatement.class);
        if (stmtExpr == null) {
            return false;
        }

        ASTMethodCall methodCall = stmtExpr.firstChild(ASTMethodCall.class);
        if (methodCall == null) {
            return false;
        }
        return NodeUtils.isLockStatementExpression(methodCall);

    }

    /**
     * Determines if a primary expression is a static SimpleDateFormat format call
     */
    private boolean isStaticSimpleDateFormatCall(
            ASTPrimaryExpression primaryExpression,
            Set<String> localSimpleDateFormatNames
    ) {
        // Check if this is a method call
        ASTMethodCall methodCall = primaryExpression.firstChild(ASTMethodCall.class);
        if (methodCall == null || !FORMAT_METHOD_NAME.equals(methodCall.getMethodName())) {
            return false;
        }
        ASTVariableAccess variableAccess = methodCall.firstChild(ASTVariableAccess.class);
       if (variableAccess == null) {
            return false;
        }

        // If this is a local variable, it's not a static violation
        String variableName = variableAccess.getName();
        if (variableName.contains(".")) {
            variableName = variableName.substring(0, variableName.lastIndexOf('.'));
        }

        return !localSimpleDateFormatNames.contains(variableName);

    }
}