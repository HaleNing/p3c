package com.alibaba.p3c.pmd.lang.java.rule.constant;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTNumericLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTStringLiteral;

/**
 * [Mandatory] 'L' instead of 'l' should be used for long or Long variable because 'l' is easily to
 * be regarded as number 1 in mistake.
 *
 * @author XiNing.Liu
 * @date 2025/03/12
 */
public class UpperEllRule extends AbstractAliRule {
    private static final String LOWERCASE_L = "l";

    public Object visit(ASTNumericLiteral node, Object data) {
        boolean isLongValue = node.isLongLiteral();
        Chars text = node.getText();
        // if it is an integer and ends with l, collects the current violation code
        if (isLongValue && text.endsWith(LOWERCASE_L)) {
            addViolationWithMessage(data, node, "java.constant.UpperEllRule.violation.msg",
                new Object[] {node.getImage()});
        }
        return super.visit(node, data);
    }

}
