package com.alibaba.p3c.pmd.lang.java.rule.flowcontrol;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.java.ast.*;
import java.util.List;

/**
 * [Mandatory] In a switch block, each case should be finished by break/return.
 * If not, a note should be included to describe at which case it will stop. Within every switch block,
 * a default statement must be present, even if it is empty.
 *
 * @author XiNing.Liu
 * @date 2025/03/16
 */
public class SwitchStatementRule extends AbstractAliRule {
    private static final String MESSAGE_KEY_PREFIX = "java.flowcontrol.SwitchStatementRule.violation";

    @Override
    public Object visit(ASTSwitchStatement node, Object data) {
        checkDefault(node, data);
        checkFallThrough(node, data);
        return super.visit(node, data);
    }

    /**
     * Check if switch statement contains default branch
     */
    private void checkDefault(ASTSwitchStatement node, Object data) {
        // Check if there's a default label in the switch statement
        boolean notDefault = node.descendants(ASTSwitchLabel.class)
                .filter(ASTSwitchLabel::isDefault)
                .count() <= 0;
        if (notDefault) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".nodefault");
        }
    }

    /**
     * Check the availability of break, return, throw, continue in case statement
     */
    private void checkFallThrough(ASTSwitchStatement node, Object data) {
        // Count all case labels (excluding default)
        int caseLabelsCount = node.descendants(ASTSwitchLabel.class)
                .filter(label -> !label.isDefault())
                .count();

        // Count all termination statements
        int terminatingStatementsCount = 0;

        // Count break statements
        terminatingStatementsCount += node.descendants(ASTBreakStatement.class).count();

        // Count return, continue, throw statements in block statements
        terminatingStatementsCount += node.descendants(ASTBlock.class)
                .flatMap(block -> block.descendants(ASTStatement.class))
                .filter(stmt -> {
                    return stmt.descendants(ASTReturnStatement.class).count() > 0 ||
                            stmt.descendants(ASTContinueStatement.class).count() > 0 ||
                            stmt.descendants(ASTThrowStatement.class).count() > 0;
                })
                .count();

        // Count empty cases (a case followed immediately by another case or default)
        terminatingStatementsCount += countEmptyCases(node);

        // If there are fewer terminating statements than case labels, some cases fall through
        if (terminatingStatementsCount < caseLabelsCount) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".notermination");
        }
    }

    /**
     * Count cases that are empty (followed directly by another case or are the last case)
     */
    private int countEmptyCases(ASTSwitchStatement node) {
        int emptyCount = 0;

        // Get all the nodes in the switch statement body
        List<JavaNode> list = node.children().toList();

        // Check for consecutive labels
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) instanceof ASTSwitchLabel &&
                    list.get(i + 1) instanceof ASTSwitchLabel) {
                emptyCount++;
            }
        }

        // Check if the last child is a switch label (empty last case)
        if (!list.isEmpty() && list.get(list.size() - 1) instanceof ASTSwitchLabel) {
            emptyCount++;
        }

        return emptyCount;
    }
}