package com.alibaba.p3c.pmd.lang.java.rule.exception;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.types.JPrimitiveType;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;

/**
 * [Recommended] If the return type is primitive, return a value of wrapper class may cause NullPointerException.
 *
 * @author XiNing.Liu
 * @date 2025/03/12
 */
public class MethodReturnWrapperTypeRule extends AbstractAliRule {

    @Override
    public Object visit(ASTMethodDeclaration methodNode, Object data) {

        // Find the primitive return type
        ASTPrimitiveType primitiveTypeOptional = methodNode.descendants(ASTType.class)
                .flatMap(type -> type.descendants(ASTPrimitiveType.class))
                .first();


        if (Objects.isNull(primitiveTypeOptional)) {
            return super.visit(methodNode, data);
        }


        JPrimitiveType.PrimitiveTypeKind kind = primitiveTypeOptional.getKind();
        // int char
        String returnTypeName = kind.getSimpleName();


        // Find the return statements with a variable name
        List<ASTReturnStatement> methodReturnNodeList
                = methodNode.descendants(ASTBlock.class).flatMap(block -> block.descendants(ASTReturnStatement.class)).toList();

        if (methodReturnNodeList.isEmpty()) {
            return super.visit(methodNode, data);
        }
        // all variable name, ["name","text","age"]
        List<ASTVariableId> astVariableIdList = methodNode.descendants(ASTLocalVariableDeclaration.class).descendants(ASTVariableId.class).toList();
        List<String> variableNameList = astVariableIdList.stream().map(ASTVariableId::getName).collect(Collectors.toList());

        for (ASTReturnStatement returnStatement : methodReturnNodeList) {

            ASTVariableAccess astVariableAccess = returnStatement.descendants(ASTPrimaryExpression.class).descendants(ASTVariableAccess.class).first();

            if (Objects.nonNull(astVariableAccess)) {
                // var name like "text" in  String text = "123"; return text;
                String name = astVariableAccess.getName();
                if (variableNameList.contains(name)) {
                    // Find the variable type, like text is String
                    ASTLocalVariableDeclaration firstLocalDecl = astVariableAccess.ancestors(ASTLocalVariableDeclaration.class).first();
                    if (Objects.nonNull(firstLocalDecl)) {
                        ASTType typeNode = firstLocalDecl.getTypeNode();
                        if (Objects.nonNull(typeNode)) {
                            JTypeMirror typeMirror = typeNode.getTypeMirror();
                            boolean classOrInterface = typeMirror.isClassOrInterface();
                            if (classOrInterface && Objects.nonNull(typeMirror.getSymbol())) {
                                ViolationUtils.addViolationWithPrecisePosition(this, methodNode, data,
                                        I18nResources.getMessage("java.exception.MethodReturnWrapperTypeRule.violation.msg",
                                                returnTypeName, typeMirror.getSymbol().getSimpleName()));
                            }
                        }
                    }

                }

            }

        }

        return super.visit(methodNode, data);
    }
}