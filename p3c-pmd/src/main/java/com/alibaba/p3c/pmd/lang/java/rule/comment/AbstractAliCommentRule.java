package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;




public abstract class AbstractAliCommentRule extends AbstractJavaRule {
    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessageWithExceptionHandled(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }


    public void addViolationWithMessage(Object data, JavaNode node, String message) {
        super.asCtx(data).addViolationWithMessage(node, I18nResources.getMessageWithExceptionHandled(message));
    }

    public void addViolationWithMessage(Object data, JavaNode node, String message, Object[] args) {
        super.asCtx(data).addViolationWithMessage(node, String.format(I18nResources.getMessageWithExceptionHandled(message), args));
    }

    public void addViolationWithMessage(Object data, JavaNode node, String message, int beginLine, int endLine) {
        super.asCtx(data).addViolationWithPosition(node, beginLine, endLine, message);
    }

}
