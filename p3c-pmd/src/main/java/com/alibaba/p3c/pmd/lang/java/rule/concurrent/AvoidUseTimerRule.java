
package com.alibaba.p3c.pmd.lang.java.rule.concurrent;

import java.util.Timer;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.types.TypeSystem;

/**
 * [Mandatory] Run multiple TimeTask by using ScheduledExecutorService rather than Timer
 * because Timer will kill all running threads in case of failing to catch exception.
 * @author XiNing.Liu
 * @date 2025/03/30
 */
public class AvoidUseTimerRule extends AbstractAliRule {
    private static final String TIMER_BINARY_NAME = "java.util.Timer";
    private static final String VIOLATION_MESSAGE = "java.concurrent.AvoidUseTimerRule.violation.msg";

    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        checkForTimer(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTExpressionStatement node, Object data) {
        ASTExpression expression = node.getExpr();
        // Look for Timer instantiation within the expression
        expression.descendants(ASTVariableDeclarator.class).forEach(var -> checkForTimer(var, data));

        return super.visit(node, data);
    }

    private void checkForTimer(ASTVariableDeclarator variableDeclarator, Object data) {
        TypeSystem typeSystem = variableDeclarator.getTypeSystem();
        JClassSymbol classSymbol = typeSystem.getClassSymbol(Timer.class);
        if (classSymbol != null && TIMER_BINARY_NAME.equals(classSymbol.getBinaryName())) {
            addViolationWithMessage(data, variableDeclarator, VIOLATION_MESSAGE);
        }
    }
}