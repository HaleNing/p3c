package com.alibaba.p3c.pmd.lang.java.rule.util;


import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


public class NodeUtils {
    public static final String LOCK_NAME = "lock";
    public static final String LOCK_INTERRUPTIBLY_NAME = "lockInterruptibly";
    public static final String UN_LOCK_NAME = "unlock";


    /**
     * Check if the expression is a wrapper type. like Long Boolean Integer etc.....
     * @param expression expression
     * @return true if wrapper type
     */
    public static boolean isWrapperType(ASTPrimaryExpression expression) {
        return TypeTestUtil.isA(Integer.class, expression)
                || TypeTestUtil.isA(Long.class, expression)
                || TypeTestUtil.isA(Boolean.class, expression)
                || TypeTestUtil.isA(Byte.class, expression)
                || TypeTestUtil.isA(Double.class, expression)
                || TypeTestUtil.isA(Short.class, expression)
                || TypeTestUtil.isA(Float.class, expression)
                || TypeTestUtil.isA(Character.class, expression);
    }

    /**
     * check if the expression is a Constant type
     * @param field
     * @return
     */
    public static boolean isConstant(ASTFieldDeclaration field) {
        return field != null && field.hasModifiers(JModifier.PUBLIC, JModifier.STATIC, JModifier.FINAL);
    }

    /**
     * Gets the type of a node that implements TypeNode interface.
     *
     * @param node The node to get type from
     * @return The type mirror of the node, or null if node is null or not a TypeNode
     */
    public static JTypeMirror getNodeType(JavaNode node) {
        if (node == null) {
            return null;
        }
        if (node instanceof TypeNode) {
            return ((TypeNode) node).getTypeMirror();
        }
        return null;
    }

    /**
     * Gets the type of a field declaration node.
     * @param astFieldDeclaration
     * @return
     */
    public static String getNodeType(ASTFieldDeclaration astFieldDeclaration) {
        ASTType typeNode = astFieldDeclaration.getTypeNode();
        if (typeNode != null) {
            return Objects.requireNonNull(typeNode.getTypeMirror().getSymbol()).getSimpleName();
        }

        return "";
    }

    /**
     * Gets the type of a local variable declaration node.
     * @param astLocalVariableDeclaration
     * @return
     */
    public static String getNodeType(ASTLocalVariableDeclaration astLocalVariableDeclaration) {
        ASTType typeNode = astLocalVariableDeclaration.getTypeNode();
        if (typeNode != null) {
            return Objects.requireNonNull(typeNode.getTypeMirror().getSymbol()).getSimpleName();
        }

        return null;
    }



    /**
     * Checks if the node represents a lock statement expression (e.g., object.wait(), lock.lock()).
     *
     * @param node The node to check
     * @return true if the node represents a lock operation
     */
    public static boolean isLockStatementExpression(JavaNode node) {
        if (!(node instanceof ASTMethodCall)) {
            return false;
        }

        ASTMethodCall methodCall = (ASTMethodCall) node;
        String methodName = methodCall.getMethodName();

        if (StringUtils.isBlank(methodName)) {
            return false;
        }

        // Check method names commonly used for locking operations
        return "lock".equals(methodName) ||
                "tryLock".equals(methodName) ||
                "wait".equals(methodName) ||
                "acquire".equals(methodName);
    }

    /**
     * Checks if the node represents an unlock statement expression (e.g., object.notify(), lock.unlock()).
     *
     * @param node The node to check
     * @return true if the node represents an unlock operation
     */
    public static boolean isUnLockStatementExpression(Node node) {
        if (!(node instanceof ASTMethodCall)) {
            return false;
        }

        ASTMethodCall methodCall = (ASTMethodCall) node;
        String methodName = methodCall.getMethodName();

        if (methodName == null) {
            return false;
        }

        // Check method names commonly used for unlocking operations
        return "unlock".equals(methodName) ||
                "notify".equals(methodName) ||
                "notifyAll".equals(methodName) ||
                "release".equals(methodName);
    }

    /**
     * Checks if a method call is on a lock-type object and matches one of the locking methods.
     *
     * @param node The method call node to check
     * @return true if the method is called on a lock-type object
     */
    public static boolean isLockTypeAndMethod(JavaNode node) {
        if (!(node instanceof ASTMethodCall)) {
            return false;
        }

        ASTMethodCall methodCall = (ASTMethodCall) node;
        String methodName = methodCall.getMethodName();

        if (methodName == null) {
            return false;
        }

        // Check the type of the qualifier (the object on which the method is called)
        JavaNode qualifier = methodCall.getQualifier();
        JTypeMirror type = getNodeType(qualifier);

        if (type == null) {
            return false;
        }

        // Check if the type is a lock type
        String typeName = type.toString();
        boolean isLockType = typeName.contains("Lock") ||
                typeName.contains("Semaphore") ||
                typeName.equals("Object");

        // Check if the method is a lock method
        boolean isLockMethod = isLockStatementExpression(node) || isUnLockStatementExpression(node);

        return isLockType && isLockMethod;
    }

    /**
     * Determines if a node represents a lock operation.
     *
     * @param node The node to check
     * @return true if the node is related to a locking mechanism
     */
    public static boolean isLockNode(JavaNode node) {
        if (node == null) {
            return false;
        }

        // Check if it's a method call on a lock object
        if (isLockTypeAndMethod(node)) {
            return true;
        }

        // Check if it's a synchronized block
        if (node instanceof ASTSynchronizedStatement) {
            return true;
        }

        // Check if it's an explicit wait/notify/notifyAll method call
        if (node instanceof ASTMethodCall) {
            ASTMethodCall methodCall = (ASTMethodCall) node;
            String methodName = methodCall.getMethodName();

            if (StringUtils.isBlank(methodName)) {
                return false;
            }

            // Check for Object's wait/notify/notifyAll methods
            return "wait".equals(methodName) ||
                    "notify".equals(methodName) ||
                    "notifyAll".equals(methodName);
        }

        return false;
    }
}
