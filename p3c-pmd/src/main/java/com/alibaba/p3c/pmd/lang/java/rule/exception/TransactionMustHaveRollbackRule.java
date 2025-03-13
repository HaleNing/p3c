package com.alibaba.p3c.pmd.lang.java.rule.exception;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.*;

/**
 * [Mandatory] Make sure to invoke the rollback if a method throws an Exception.
 *
 * @author XiNing.Liu
 * @date 2025/03/13
 */
public class TransactionMustHaveRollbackRule extends AbstractAliRule {
    private static final String TRANSACTIONAL_ANNOTATION_NAME = "Transactional";

    private static final String MESSAGE_KEY_PREFIX = "java.exception.TransactionMustHaveRollbackRule.violation.msg";

    @Override
    public Object visit(ASTMethodDeclaration methodDeclaration, Object data) {
        List<ASTClassType> classTypeList = methodDeclaration.descendants(ASTAnnotation.class)
                .flatMap(astAnnotation -> astAnnotation.descendants(ASTClassType.class)).toList();
        List<String> annotationClassNameList = classTypeList.stream().map(ASTClassType::getSimpleName).collect(Collectors.toList());

        if (annotationClassNameList.contains(TRANSACTIONAL_ANNOTATION_NAME)) {
            return super.visit(methodDeclaration, data);
        }
        ASTThrowsList throwsList = methodDeclaration.getThrowsList();
        List<ASTThrowStatement> throwStatementList = methodDeclaration.descendants(ASTThrowStatement.class).toList();

        boolean lackRollbackAnnotation = (Objects.nonNull(throwsList) && Boolean.FALSE.equals(throwsList.isEmpty()))
                || Boolean.FALSE.equals(throwStatementList.isEmpty());
        if (lackRollbackAnnotation) {
            addViolationWithMessage(data, methodDeclaration, MESSAGE_KEY_PREFIX,
                    new Object[]{methodDeclaration.getName()});
        }

        return super.visit(methodDeclaration, data);
    }


}