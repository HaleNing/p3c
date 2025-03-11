package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import net.sourceforge.pmd.lang.java.ast.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [Mandatory] Javadoc should be used for classes, class variables and methods.
 * The format should be '\/** comment *\/', rather than '// xxx'.
 *
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class CommentsMustBeJavadocFormatRule extends AbstractAliCommentRule {

    private static final String MESSAGE_KEY_PREFIX = "java.comment.CommentsMustBeJavadocFormatRule.violation.msg";


    /**
     * Check if the class or interface comment is having Javadoc comment or not.
     * @param decl
     * @param data
     * @return
     */
    @Override
    public Object visit(final ASTClassDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        if (Objects.nonNull(javadocComment)) {
            checkComment(decl, javadocComment, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".class", decl.getImage()));
        }
        return super.visit(decl, data);
    }


    /**
     * Check if the Constructor comment is having Javadoc comment or not.
     * @param decl
     * @param data
     * @return
     */
    @Override
    public Object visit(final ASTConstructorDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        if (Objects.nonNull(javadocComment)) {

            checkComment(decl, javadocComment, data, () -> {
                String constructorName = decl.getName();

                if (decl.getFormalParameters().isEmpty()) {
                    return I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".constructor.default", constructorName);
                }

                List<ASTFormalParameter> formalParameters = decl.getFormalParameters().descendants(ASTFormalParameter.class).toList();

                List<String> strings = new ArrayList<>(formalParameters.size());

                for (ASTFormalParameter formalParameter : formalParameters) {
                    strings.add(formalParameter.getFirstToken().getImage() + " " + formalParameter.getLastToken().getImage());
                }
                return I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".constructor.parameter", constructorName, StringUtils.join(strings, ","));
            });
        }
        return super.visit(decl, data);
    }


    /**
     * Check if the method comment is having Javadoc comment or not.
     * @param decl
     * @param data
     * @return
     */
    @Override
    public Object visit(final ASTMethodDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        if (Objects.nonNull(javadocComment)) {
            checkComment(decl, javadocComment, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".method", decl.getName()));
        }
        return super.visit(decl, data);
    }


    /**
     * Check if the field comment is having Javadoc comment or not.
     * @param decl
     * @param data
     * @return
     */
    @Override
    public Object visit(final ASTFieldDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        if (Objects.nonNull(javadocComment)) {
            checkComment(decl, javadocComment, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".field", VariableUtils.getVariableNameByASTFieldDeclaration(decl)));
        }
        return super.visit(decl, data);
    }

    /**
     * Check if the enum comment is having Javadoc comment or not.
     * @param decl
     * @param data
     * @return
     */
    @Override
    public Object visit(final ASTEnumDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        if (Objects.nonNull(javadocComment)) {
            checkComment(decl, javadocComment, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".enum", decl.getImage()));
        }
        return super.visit(decl, data);
    }

    /**
     * Check if the ASTCompilationUnit root is having Javadoc comment or not.
     * @param cUnit
     * @param data
     * @return
     */
    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        return super.visit(cUnit, data);
    }

    /**
     * Check if the comment is having Javadoc comment or not. If not, then add violation.
     * @param decl
     * @param comment
     * @param data
     * @param maker
     */
    private void checkComment(JavaNode decl, JavadocComment comment, Object data, MessageMaker maker) {
        if (Boolean.FALSE.equals(comment.hasJavadocContent())) {
            addViolationWithMessage(data, decl, maker.make(), comment.getReportLocation().getStartLine(), comment.getReportLocation().getEndLine());
        }
    }


    /**
     * Generate rule violation message.
     */
    interface MessageMaker {
        /**
         * Generate violation message.
         *
         * @return message
         */
        String make();
    }
}
