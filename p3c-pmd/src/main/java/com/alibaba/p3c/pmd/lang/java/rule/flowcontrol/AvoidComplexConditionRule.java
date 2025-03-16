package com.alibaba.p3c.pmd.lang.java.rule.flowcontrol;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

/**
 * [Mandatory] Do not use complicated statements in conditional statements (except for frequently used methods
 * like getXxx/isXxx). Use boolean variables to store results of complicated statements temporarily will increase
 * the code's readability.
 *
 * @author XiNing.Liu
 * @date 2025/03/16
 */
public class AvoidComplexConditionRule extends AbstractJavaRule {
//  const getName isVip
    private static final String preGet = "get";
    private static final String preIs = "is";

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        // Check the condition expression of the if statement
        checkComplexCondition(node.getCondition(), data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTConditionalExpression node, Object data) {
        // Check the condition of the ternary expression
        checkComplexCondition(node.getCondition(), data);
        return super.visit(node, data);
    }

    private void checkComplexCondition(ASTExpression condition, Object data) {
        // contains && or ||
        List<ASTInfixExpression> infixExpressionList = condition.descendants(ASTInfixExpression.class).toList();
        long infixExpressCount = infixExpressionList.stream().filter(item -> BinaryOp.CONDITIONAL_OPS.contains(item.getOperator())
                || BinaryOp.COMPARISON_OPS.contains(item.getOperator())
                || BinaryOp.EQUALITY_OPS.contains(item.getOperator())).count();


        // maybe contains getName isVip
        List<ASTMethodCall> methodCallList = condition.descendants(ASTMethodCall.class).toList();
        boolean anyMatch = methodCallList.stream().anyMatch(item -> item.getMethodName().contains(preGet) ||
                item.getMethodName().contains(preIs));
        // three is the minimum number of conditions
        if (infixExpressCount > 3 && anyMatch) {
            ViolationUtils.addViolationWithPrecisePosition(this, condition, data,
                    I18nResources.getMessage("java.flowcontrol.AvoidComplexConditionRule.violation.msg"));
        }
    }
}