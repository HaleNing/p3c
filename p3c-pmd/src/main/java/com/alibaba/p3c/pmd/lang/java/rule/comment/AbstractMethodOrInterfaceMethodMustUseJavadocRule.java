package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.java.ast.*;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * [Mandatory] Abstract methods (including methods in interface) should be commented by Javadoc.
 * Javadoc should include method instruction, description of parameters, return values and possible exception.
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class AbstractMethodOrInterfaceMethodMustUseJavadocRule extends AbstractAliCommentRule {

    private static final String MESSAGE_KEY_PREFIX
            = "java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.violation.msg";

    private static final Pattern RETURN_PATTERN = Pattern.compile(".*@return.*", Pattern.DOTALL);

    @Override
    public Object visit(ASTClassDeclaration decl, Object data) {
        if (decl.isAbstract()) {
            List<ASTMethodDeclaration> methods = decl.descendants(ASTMethodDeclaration.class).toList();
            for (ASTMethodDeclaration method : methods) {
                if (Boolean.FALSE.equals(method.isAbstract())) {
                    continue;
                }
                JavadocComment javadocComment = method.getJavadocComment();
                if (Objects.isNull(javadocComment)) {
                    ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                            I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".abstract", method.getName()));
                } else {
                    this.checkMethodCommentFormat(method, data, javadocComment);
                }
            }
        }
        if (Boolean.FALSE.equals(decl.isInterface())) {
            return super.visit(decl, data);
        }
        List<ASTMethodDeclaration> methodNodes = decl.descendants(ASTMethodDeclaration.class).toList();


        for (ASTMethodDeclaration node : methodNodes) {
            JavadocComment javadocComment = node.getJavadocComment();
            if (Objects.isNull(javadocComment)) {
                ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                        I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".abstract", node.getName()));
            } else {
                this.checkMethodCommentFormat(node, data, javadocComment);
            }
        }
        return super.visit(decl, data);
    }


    private void checkMethodCommentFormat(ASTMethodDeclaration method, Object data, JavadocComment docComment) {
        // 获取方法注释
        if (docComment == null || docComment.getText().isEmpty()) {
            // 没有JavaDoc注释，添加违规
            ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                    I18nResources.getMessage("java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.violation.msg"));
            return;
        }

        // 获取注释内容
        Chars commentContent = docComment.getText();
        // 检查非void方法是否有@return标签
        if (!method.isVoid() && !RETURN_PATTERN.matcher(commentContent).matches()) {
            ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                    I18nResources.getMessage("java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.return.violation.msg"));
        }

        // 检查参数文档
        method.descendants(ASTFormalParameter.class).forEach(parameter -> {
            ASTVariableId paramId = parameter.getVarId();
            String paramName = paramId.getName();

            // 检查是否有对应参数的@param标签
            if (!Pattern.compile("@param\\s+" + paramName + "\\s+.+").matcher(commentContent).find()) {
                ViolationUtils.addViolationWithPrecisePosition(this, parameter, data,
                        I18nResources.getMessage("java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.parameter.violation.msg", paramName));
            }
        });

        // 检查异常文档
        method.descendants(ASTThrowsList.class)
                .flatMap(nameList -> nameList.descendants(ASTClassType.class))
                .forEach(exName -> {
                    String simpleExName = exName.getSimpleName();
                    // 检查是否有对应异常的@throws标签
                    Pattern throwsPattern = Pattern.compile("@throws\\s+" + simpleExName + "\\s+.+");
                    if (!throwsPattern.matcher(commentContent).find()) {
                        ViolationUtils.addViolationWithPrecisePosition(this, exName, data,
                                I18nResources.getMessage("java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.exception.violation.msg", simpleExName));
                    }
                });
    }

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        return super.visit(cUnit, data);
    }

}
