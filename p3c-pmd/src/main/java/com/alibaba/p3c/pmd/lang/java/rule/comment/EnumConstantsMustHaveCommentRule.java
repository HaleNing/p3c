
package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavadocComment;

import java.util.Objects;

/**
 * [Mandatory] All enumeration type fields should be commented as Javadoc style.
 *
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class EnumConstantsMustHaveCommentRule extends AbstractAliCommentRule {


    /**
     * Visits enum declarations to check if they have proper Javadoc comments.
     * Reports violations if the enum declaration is missing a Javadoc comment
     * or if the Javadoc comment is empty.
     *
     * @param enumDeclaration the enum declaration node being visited
     * @param data context information
     * @return the result of the visit
     */
    @Override
    public Object visit(ASTEnumDeclaration enumDeclaration, Object data) {


        JavadocComment javadocComment = enumDeclaration.getJavadocComment();
        if (Objects.isNull(javadocComment)) {
            addViolationWithMessage(data, enumDeclaration,
                    I18nResources.getMessage("java.comment.EnumConstantsMustHaveCommentRule.violation.msg", enumDeclaration.getSimpleName()));
        }
        if (Objects.nonNull(javadocComment) && !javadocComment.hasJavadocContent()) {
            addViolationWithMessage(data, enumDeclaration,
                    I18nResources.getMessage("java.comment.EnumConstantsMustHaveCommentRule.violation.msg", enumDeclaration.getSimpleName()));
        }
        return super.visit(enumDeclaration, data);
    }


    /**
     * Visits enum constants to check if they have proper Javadoc comments.
     * Reports violations if the enum constant is missing a Javadoc comment
     * or if the Javadoc comment is empty.
     *
     * @param enumConstant the enum constant node being visited
     * @param data context information
     * @return the result of the visit
     */
    @Override
    public Object visit(ASTEnumConstant enumConstant, Object data) {

        JavadocComment javadocComment = enumConstant.getJavadocComment();
        if (Objects.isNull(javadocComment)) {
            addViolationWithMessage(data, enumConstant,
                    I18nResources.getMessage("java.comment.EnumConstantsMustHaveCommentRule.violation.msg", enumConstant.getName()));
        }
        if (Objects.nonNull(javadocComment) && !javadocComment.hasJavadocContent()) {
            addViolationWithMessage(data, enumConstant,
                    I18nResources.getMessage("java.comment.EnumConstantsMustHaveCommentRule.violation.msg", enumConstant.getName()));
        }

        return super.visit(enumConstant, data);
    }


}
