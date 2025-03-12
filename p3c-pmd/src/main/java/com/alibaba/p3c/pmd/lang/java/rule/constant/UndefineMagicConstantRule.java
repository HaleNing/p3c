package com.alibaba.p3c.pmd.lang.java.rule.constant;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.java.ast.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [Mandatory] Magic values, except for predefined, are forbidden in coding.
 *
 * @author XiNing.Liu
 * @date 2025/03/12
 */
public class UndefineMagicConstantRule extends AbstractAliRule {

    /**
     * white list for undefined variable, may be added
     */
    private final static List<String> LITERAL_WHITE_LIST = NameListConfig.NAME_LIST_SERVICE.getNameList(
            UndefineMagicConstantRule.class.getSimpleName(), "LITERAL_WHITE_LIST");

    /**
     * An undefined that belongs to non-looped if statements
     *
     * @param compilationUnitNode compilation unit
     * @param data rule context
     */
    @Override
    public Object visit(ASTCompilationUnit compilationUnitNode, Object data) {
        // removed repeat magic value, to prevent the parent class to find sub-variable nodes when there is a repeat
        List<ASTLiteral> currentLiterals = new ArrayList<>();

        // Find literals that are not part of variable initializers
        compilationUnitNode.descendants(ASTLiteral.class)
                .filter(literal -> Boolean.FALSE.equals(isInVariableInitializer(literal)))
                .forEach(literal -> {
                    if (inBlackList(literal) && !currentLiterals.contains(literal)) {
                        currentLiterals.add(literal);
                        String imageReplace = StringUtils.replace(literal.getImage(), "{", "'{");
                        addViolationWithMessage(data, literal, "java.constant.UndefineMagicConstantRule.violation.msg", new Object[]{imageReplace});
                    }
                });

        return super.visit(compilationUnitNode, data);
    }

    /**
     * Check if the literal is part of a variable initializer
     *  like an int threshold = 30;
     */
    private boolean isInVariableInitializer(ASTLiteral literal) {
        ASTVariableDeclarator astVariableDeclarator = literal.ancestors(ASTVariableDeclarator.class).first();
        if (astVariableDeclarator != null) {
            ASTExpression initializer = astVariableDeclarator.getInitializer();
            return initializer != null && initializer.getBeginLine() == literal.getBeginLine();
        }
        return false;
    }

    /**
     * Undefined variables are in the blacklist
     *
     * @param literal
     * @return
     */
    private boolean inBlackList(ASTLiteral literal) {
        Chars literalText = literal.getLiteralText();

        int lineNum = literal.getBeginLine();
        // name is null,bool literal，belongs to white list
        if (Objects.isNull(literalText)) {
            return false;
        }
        // filter white list
        for (String whiteItem : LITERAL_WHITE_LIST) {
            if (whiteItem.equals(literalText.toString())) {
                return false;
            }
        }

        // Check if the magic literal is in an if statement
        ASTIfStatement ifStatement = literal.ancestors(ASTIfStatement.class).first();
        if (ifStatement != null && lineNum == ifStatement.getBeginLine()) {
            // Check if the if statement is inside a loop
            boolean inLoop = Objects.nonNull(ifStatement.ancestors(ASTForStatement.class).first()) ||
                    Objects.nonNull(ifStatement.ancestors(ASTWhileStatement.class).first());

            return Boolean.FALSE.equals(inLoop);
        }

        // judge magic value belongs to for statement
        ASTForStatement blackForStatement = literal.ancestors(ASTForStatement.class).first();
        if (blackForStatement != null && lineNum == blackForStatement.getBeginLine()) {
            return true;
        }

        // judge magic value belongs to while statement
        ASTWhileStatement blackWhileStatement = literal.ancestors(ASTWhileStatement.class).first();
        ASTForStatement blackForloopStatement = literal.ancestors(ASTForStatement.class).first();

        return (blackWhileStatement != null && lineNum == blackWhileStatement.getBeginLine()) ||
                (blackForloopStatement != null && lineNum == blackForloopStatement.getBeginLine());

    }
}