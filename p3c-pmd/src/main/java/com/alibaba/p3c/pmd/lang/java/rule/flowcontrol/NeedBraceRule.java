package com.alibaba.p3c.pmd.lang.java.rule.flowcontrol;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;

import java.util.Objects;

/**
 * [Mandatory] Braces are used with if, else, for, do and while statements, even if the body contains only a single
 * statement. Avoid using the following example:
 * <pre>
 * if (condition) statements;
 * </pre>
 *
 * @author XiNing.Liu
 * @date 2025/03/16
 */
public class NeedBraceRule extends AbstractAliRule {

    private static final String MESSAGE_KEY = "java.flowcontrol.NeedBraceRule.violation.msg";

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        // SwitchStatement without {} fail by compilaton, no need to check here
        ASTBlock ifBlock = node.children(ASTBlock.class).first();
        if (Objects.isNull(ifBlock)) {
            addViolationWithMessage(data, node, MESSAGE_KEY,
                    new Object[]{node.getCondition().toString()});
        }
        if (node.hasElse()) {
            // IfStatement with else have 2 expression blocks, should never throws NPE
            ASTStatement elseBranch = node.getElseBranch();
            if (Objects.nonNull(elseBranch)) {
                if (elseBranch.descendants(ASTBlock.class).isEmpty()) {
                    addViolationWithMessage(data, elseBranch, MESSAGE_KEY, new Object[]{"else"});
                }
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTForStatement node, Object data) {
        ASTBlock block = node.children(ASTBlock.class).first();

        if (Objects.isNull(block)) {
            addViolationWithMessage(data, node, MESSAGE_KEY, new Object[]{"for"});
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        ASTBlock block = node.children(ASTBlock.class).first();

        if (Objects.isNull(block)) {
            addViolationWithMessage(data, node, MESSAGE_KEY, new Object[]{"while"});
        }
        return super.visit(node, data);
    }

}
