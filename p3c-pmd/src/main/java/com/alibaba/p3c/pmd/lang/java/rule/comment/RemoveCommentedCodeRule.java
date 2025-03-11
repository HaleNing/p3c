package com.alibaba.p3c.pmd.lang.java.rule.comment;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.java.ast.*;

/**
 * [Recommended] Codes or configuration that is noticed to be obsoleted should be resolutely removed from projects.
 *
 * @author XiNing.Liu
 * @date 2025/03/11
 */
public class RemoveCommentedCodeRule extends AbstractAliCommentRule {

    private static final Pattern SUPPRESS_PATTERN = Pattern.compile("\\s*///.*", Pattern.DOTALL);
    private static final Pattern PRE_TAG_PATTERN = Pattern.compile(".*<pre>.*", Pattern.DOTALL);
    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*import\\s(static\\s)?(\\w*\\.)*\\w*;.*",
            Pattern.DOTALL);
    private static final Pattern FIELD_PATTERN = Pattern.compile(".*private\\s+(\\w*)\\s+(\\w*);.*", Pattern.DOTALL);
    private static final Pattern METHOD_PATTERN = Pattern.compile(".*(public|protected|private)\\s+\\w+\\s+\\w+\\(.*\\)\\s+\\{.*", Pattern.DOTALL);
    private static final Pattern STATEMENT_PATTERN = Pattern.compile(".*\\.\\w+\\(.*\\);\n.*", Pattern.DOTALL);

    private JavaComment lastComment;

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        checkCommentsBetweenDeclarations(cUnit, data);
        return super.visit(cUnit, data);
    }

    protected void checkCommentsBetweenDeclarations(ASTCompilationUnit rootUnit, Object data) {
        // Create a sorted list containing both nodes and their line numbers
        List<Object[]> sortedItems = createSortedItemsList(rootUnit);

        boolean suppressWarning = false;
        CommentPatternEnum commentPattern = CommentPatternEnum.NONE;

        for (Object[] item : sortedItems) {
            Object value = item[1];

            if (value instanceof JavaNode && Boolean.FALSE.equals(value instanceof JavaComment)) {
                JavaNode node = (JavaNode) value;
                // Add violation on the node after comment
                if (lastComment != null && isCommentBefore(lastComment, node)) {
                    // Find code comment, but need to filter some cases
                    if (!CommentPatternEnum.NONE.equals(commentPattern)) {
                        // Check statement pattern only in method
                        boolean statementOutsideMethod = CommentPatternEnum.STATEMENT.equals(commentPattern)
                                && !(node instanceof ASTStatement);
                        if (!statementOutsideMethod) {
                            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                                    I18nResources.getMessage("java.comment.RemoveCommentedCodeRule.violation.msg"));
                        }
                    }
                    lastComment = null;
                }

                // Reset data after each node
                suppressWarning = false;
                commentPattern = CommentPatternEnum.NONE;

            } else if (value instanceof JavaComment) {
                lastComment = (JavaComment) value;
                Chars content = lastComment.getText();

                if (!suppressWarning) {
                    suppressWarning = SUPPRESS_PATTERN.matcher(content).matches();
                }

                if (!suppressWarning && CommentPatternEnum.NONE.equals(commentPattern)) {
                    commentPattern = this.scanCommentedCode(content);
                }
            }
        }
    }

    /**
     * Creates a sorted list containing both nodes and comments with their line numbers
     */
    protected List<Object[]> createSortedItemsList(ASTCompilationUnit cUnit) {
        SortedMap<Integer, Node> nodesByLineNumber = orderedDeclarations(cUnit);
        List<JavaComment> comments = cUnit.getComments();

        // Create a merged, sorted list of nodes and comments
        List<Object[]> sortedItems = new ArrayList<>();

        // Add all nodes to the list
        for (Entry<Integer, Node> entry : nodesByLineNumber.entrySet()) {
            sortedItems.add(new Object[]{entry.getKey(), entry.getValue()});
        }

        // Add all comments to the list
        for (JavaComment comment : comments) {
            sortedItems.add(new Object[]{comment.getReportLocation().getStartLine(), comment});
        }

        // Sort the list by line number
        sortedItems.sort(Comparator.comparing(a -> ((Integer) a[0])));
        return sortedItems;
    }

    /**
     * Common Situations, check in following order:
     * 1. commented import
     * 2. commented field
     * 3. commented method
     * 4. commented statement
     */
    protected CommentPatternEnum scanCommentedCode(Chars content) {
        CommentPatternEnum pattern = CommentPatternEnum.NONE;

        if (PRE_TAG_PATTERN.matcher(content).matches()) {
            return pattern;
        }

        if (IMPORT_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.IMPORT;
        } else if (FIELD_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.FIELD;
        } else if (METHOD_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.METHOD;
        } else if (STATEMENT_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.STATEMENT;
        }

        return pattern;
    }

    protected SortedMap<Integer, Node> orderedDeclarations(ASTCompilationUnit cUnit) {
        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        // Import declarations
        List<ASTImportDeclaration> importDecl = cUnit.descendants(ASTImportDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, importDecl);

        // Class declarations
        List<ASTClassDeclaration> classDecls = cUnit.descendants(ASTClassDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, classDecls);

        // Interface declarations (using TypeDeclaration since PMD7 structure)
        List<ASTTypeDeclaration> typeDecls = cUnit.descendants(ASTTypeDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, typeDecls);

        // Field declarations
        List<ASTFieldDeclaration> fields = cUnit.descendants(ASTFieldDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, fields);

        // Method declarations
        List<ASTMethodDeclaration> methods = cUnit.descendants(ASTMethodDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, methods);

        // Constructor declarations
        List<ASTConstructorDeclaration> constructors = cUnit.descendants(ASTConstructorDeclaration.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, constructors);

        // Statements (generic instead of BlockStatement)
        List<ASTStatement> statements = cUnit.descendants(ASTStatement.class).toList();
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, statements);

        return itemsByLineNumber;
    }

    private boolean isCommentBefore(JavaComment comment, JavaNode node) {
        FileLocation commentLoc = comment.getReportLocation();
        FileLocation nodeLoc = node.getReportLocation();

        return commentLoc.getEndLine() < nodeLoc.getStartLine() ||
                (commentLoc.getEndLine() == nodeLoc.getStartLine() &&
                        commentLoc.getEndColumn() < nodeLoc.getStartColumn());
    }

    enum CommentPatternEnum {
        /**
         * comment has code pattern
         */
        IMPORT,
        FIELD,
        METHOD,
        STATEMENT,
        NONE
    }
}