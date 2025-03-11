package com.alibaba.p3c.pmd.lang.java.rule.comment;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.java.ast.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * [Mandatory] Single line comments in a method should be put above the code to be commented, by using // and
 * multiple lines by using \/* *\/. Alignment for comments should be noticed carefully.
 *
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class AvoidCommentBehindStatementRule extends AbstractAliCommentRule {

    @Override
    public Object visit(ASTCompilationUnit rootUnit, Object data) {
        SortedMap<Integer, Node> itemsByLineNumber = orderedCommentsAndExpressions(rootUnit);
        List<JavaComment> rootUnitComments = rootUnit.getComments();

        // Group comments by line number for faster lookup
        Map<Integer, List<JavaComment>> commentsByLine = new HashMap<>();
        for (JavaComment comment : rootUnitComments) {
            int line = comment.getReportLocation().getStartLine();
            commentsByLine.computeIfAbsent(line, k -> new ArrayList<>()).add(comment);
        }

        // Process each node, only checking comments on the same line
        for (Entry<Integer, Node> entry : itemsByLineNumber.entrySet()) {
            Node node = entry.getValue();
            if (node instanceof JavaNode) {
                JavaNode javaNode = (JavaNode) node;
                FileLocation nodeLoc = javaNode.getReportLocation();
                int nodeLine = nodeLoc.getStartLine();
                int nodeColumn = nodeLoc.getStartColumn();

                // Only check comments on the same line
                List<JavaComment> sameLineComments = commentsByLine.get(nodeLine);
                if (sameLineComments != null) {
                    for (JavaComment comment : sameLineComments) {
                        if (comment.getReportLocation().getStartColumn() > nodeColumn) {
                            ViolationUtils.addViolationWithPrecisePosition(this, javaNode, data,
                                    I18nResources.getMessage("java.comment.AvoidCommentBehindStatementRule.violation.msg"));
                            break;
                        }
                    }
                }
            }
        }

        return super.visit(rootUnit, data);
    }
    /**
     * Check comments behind nodes.
     *
     * @param rootUnit compilation unit
     * @return sorted comments and expressions
     */
    protected SortedMap<Integer, Node> orderedCommentsAndExpressions(ASTCompilationUnit rootUnit) {

        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        // expression nodes
        List<ASTExpression> expressionNodes = rootUnit.descendants(ASTExpression.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, expressionNodes);

        // filed declaration nodes
        List<ASTFieldDeclaration> fieldNodes = rootUnit.descendants(ASTFieldDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, fieldNodes);

        // enum constant nodes
        List<ASTEnumConstant> enumConstantNodes = rootUnit.descendants(ASTEnumConstant.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, enumConstantNodes);


        return itemsByLineNumber;
    }

}
