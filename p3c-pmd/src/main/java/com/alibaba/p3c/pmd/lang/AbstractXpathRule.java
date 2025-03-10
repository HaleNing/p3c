
package com.alibaba.p3c.pmd.lang;

import com.alibaba.p3c.pmd.I18nResources;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.rule.AbstractRule;



public abstract class AbstractXpathRule extends AbstractRule {



    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessageWithExceptionHandled(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }


    /**
     * 让子类直接调用抽象类自己定义的方法
     * @param data
     * @param node
     * @param message
     */
    public void addViolationWithMessage(Object data, JavaNode node, String message) {
        super.asCtx(data).addViolationWithMessage(node, I18nResources.getMessageWithExceptionHandled(message));
    }

    /**
     * 让子类直接调用抽象类自己定义的方法
     * @param data
     * @param node
     * @param message
     * @param args
     */
    public void addViolationWithMessage(Object data, JavaNode node, String message, Object[] args) {
        super.asCtx(data).addViolationWithMessage(node,
                String.format(I18nResources.getMessageWithExceptionHandled(message), args));
    }

    /**
     * 让子类直接调用抽象类自己定义的方法
     * @param data
     * @param node
     * @param arg
     */
    public void addViolation(Object data, JavaNode node, String arg) {
        super.asCtx(data).addViolation(node, arg);

    }

}
