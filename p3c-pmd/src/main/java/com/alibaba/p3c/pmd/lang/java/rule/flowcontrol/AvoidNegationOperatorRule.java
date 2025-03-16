
package com.alibaba.p3c.pmd.lang.java.rule.flowcontrol;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.java.ast.*;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * [Recommended]Avoid using the negation operator '!'.
 * Note: The negation operator is not easy to be quickly understood. There must be a positive
 * way to represent the same logic.
 *
 * @author XiNing.Liu
 * @date 2025/03/16
 */
public class AvoidNegationOperatorRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        Boolean isHave = checkOperator(node);
        if (isHave) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                    I18nResources.getMessage("java.flowcontrol.AvoidNegationOperatorRule.violation.msg"));
        }
        return super.visit(node, data);

    }

    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        Boolean isHave = checkOperator(node);
        if (isHave) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                    I18nResources.getMessage("java.flowcontrol.AvoidNegationOperatorRule.violation.msg"));
        }
        return super.visit(node, data);

    }


    private Boolean checkOperator(JavaNode condition) {
        int neCount = condition.descendants(ASTUnaryExpression.class).filter(item -> {
            return UnaryOp.NEGATION.equals(item.getOperator());
        }).count();
        int neqCount = condition.descendants(ASTInfixExpression.class).filter(item -> {
            return BinaryOp.NE.equals(item.getOperator());
        }).count();


        return neqCount > 0 || neCount > 0;
    }

}
