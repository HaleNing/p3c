package com.alibaba.p3c.pmd.lang.java.rule.exception;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFinallyClause;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;

/**
 * [Mandatory] Never use return within a finally block.
 * A return statement in a finally block will cause exception or result
 * in a discarded return value in the try-catch block.
 *
 * @author XiNing.Liu
 * @date 2025/03/12
 */
public class AvoidReturnInFinallyRule extends AbstractAliRule {

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // Find all finally statements
        node.descendants(ASTFinallyClause.class).forEach(finallyStatement -> {
            // Find return statements within this finally block
            finallyStatement.descendants(ASTReturnStatement.class).forEach(returnStatement -> {
                addViolationWithMessage(data, returnStatement, "java.exception.AvoidReturnInFinallyRule.violation.msg");
            });
        });

        return super.visit(node, data);
    }
}