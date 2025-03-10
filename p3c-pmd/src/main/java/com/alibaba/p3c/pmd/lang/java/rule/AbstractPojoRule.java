package com.alibaba.p3c.pmd.lang.java.rule;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.util.PojoUtils;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;

/**
 * Base class for POJO
 */
public abstract class AbstractPojoRule extends AbstractAliRule {

    /**
     * filter for all POJO class, skip if no POJO.
     * consider inner class
     *
     * @param node compilation unit
     * @param data rule context
     * @return result
     */
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // proceed if contains POJO
        if (hasPojoInJavaFile(node)) {
            return super.visit(node, data);
        }
        return data;
    }

    /**
     * check contains POJO
     * @param node compilation unit
     * @return true if file contains POJO classes
     */
    private boolean hasPojoInJavaFile(ASTCompilationUnit node) {
        // PMD 7.0 中 findDescendantsOfType 方法移到了节点本身
        // 需要分别检查类声明和接口声明
        List<ASTClassDeclaration> classDeclarations = node.descendants(ASTClassDeclaration.class).toList();
        for (ASTClassDeclaration classNode : classDeclarations) {
            if (isPojo(classNode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a class declaration represents a POJO
     */
    protected boolean isPojo(ASTClassDeclaration node) {
        return node != null && PojoUtils.isPojo(node.getSimpleName());
    }

}