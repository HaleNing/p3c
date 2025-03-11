/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.java.ast.*;

import java.util.Objects;
import java.util.regex.Pattern;


/**
 * [Mandatory] Every class should include information of author(s) and date.
 *
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class ClassMustHaveAuthorRule extends AbstractAliCommentRule {

    private static final Pattern AUTHOR_PATTERN = Pattern.compile(".*@[Aa]uthor.*",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final String MESSAGE_KEY_PREFIX = "java.comment.ClassMustHaveAuthorRule.violation.msg";

    /**
     * Immediately return after visiting class/interface/enum/annotation,
     * so that we don't need to deal with inner class/interface/enum/annotation declarations.
     *
     * @param decl node
     * @param data ruleContext
     * @return result
     */
    @Override
    public Object visit(ASTClassDeclaration decl, Object data) {
        // If a CompilationUnit has multi class definition, only the public one will be checked.
        if (decl.hasModifiers(JModifier.PUBLIC)) {
            JavadocComment javadocComment = decl.getJavadocComment();
            checkAuthorComment(decl, data, javadocComment);
        }
        return data;
    }

    @Override
    public Object visit(ASTEnumDeclaration decl, Object data) {
        // Exclude inner enum
        if (Boolean.FALSE.equals(decl.hasModifiers(JModifier.PUBLIC))) {
            return super.visit(decl, data);
        }

        // Inner enum should have author tag in outer class.
        JavaNode parent = decl.getParent();
        while (parent != null && !(parent instanceof ASTClassDeclaration)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            return super.visit(decl, data);
        }

        JavadocComment javadocComment = decl.getJavadocComment();
        checkAuthorComment(decl, data, javadocComment);
        return data;
    }

    @Override
    public Object visit(ASTAnnotationTypeDeclaration decl, Object data) {
        JavadocComment javadocComment = decl.getJavadocComment();
        checkAuthorComment(decl, data, javadocComment);
        return data;
    }

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        return super.visit(cUnit, data);
    }


    /**
     * Check if node's comment contains author tag.
     *
     * @param decl           node
     * @param data           ruleContext
     * @param javadocComment
     */
    public void checkAuthorComment(JavaNode decl, Object data, JavadocComment javadocComment) {

        if (Objects.isNull(javadocComment)) {
            ViolationUtils.addViolationWithPrecisePosition(this, decl, data,
                    I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".comment", decl.getImage()));
        } else {
            Chars text = javadocComment.getText();
            boolean hasAuthor = AUTHOR_PATTERN.matcher(text).matches();
            if (Boolean.FALSE.equals(hasAuthor)) {
                ViolationUtils.addViolationWithPrecisePosition(this, decl, data,
                        I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".author", decl.getImage()));
            }
        }
    }
}
