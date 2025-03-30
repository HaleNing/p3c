package com.alibaba.p3c.pmd.lang.java.rule.concurrent;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 6.13 [Recommended] Avoid using Random instance by multiple threads.
 * Although it is safe to share this instance, competition on the same seed will damage performance.
 * Note: Random instance includes instances of java.util.Random and Math.random().
 *
 * @author XiNing.Liu
 * @date 2025/03/30
 */
public class AvoidConcurrentCompetitionRandomRule extends AbstractAliRule {

    // Store the symbols of static Random fields found in the concurrent class
    private final Set<JVariableSymbol> staticRandomFieldSymbols = new HashSet<>();
    private static final String MESSAGE_KEY_PREFIX = "java.concurrent.AvoidConcurrentCompetitionRandomRule";

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // Clear state for each new file
        staticRandomFieldSymbols.clear();
        return super.visit(node, data); // Continue traversal
    }


    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        // 1. Check if the class is likely used in a concurrent context
        if (!isConcurrentContext(node)) {
            // Not extending Thread or implementing Runnable, skip further checks for this class
            // but continue visiting nested classes
            return super.visit(node, data);
        }

        // Clear symbols specific to this class before processing its contents
        // (Handles nested concurrent classes potentially reusing the rule instance)
        Set<JVariableSymbol> previousSymbols = new HashSet<>(staticRandomFieldSymbols);
        staticRandomFieldSymbols.clear(); // Clear for this specific class context

        // 2. Find static fields of type java.util.Random within this class
        List<ASTFieldDeclaration> fieldDeclarations = node.descendants(ASTFieldDeclaration.class).toList();
        for (ASTFieldDeclaration fieldDecl : fieldDeclarations) {
            if (fieldDecl.isStatic() && TypeTestUtil.isA(java.util.Random.class, fieldDecl.getTypeNode())) {
                // Found a static Random field. Store its symbol.
                // Assuming only one variable per declaration for simplicity here.
                // A more robust implementation would handle multiple variables declared together.
                if (!fieldDecl.getVarIds().toList().isEmpty()) {
                    // Note: getSymbol() is on the declarator ID
                    List<ASTVariableId> variableIds = fieldDecl.getVarIds().toList();
                    JVariableSymbol fieldSymbol = variableIds.get(0).getSymbol();
                    staticRandomFieldSymbols.add(fieldSymbol);
                }
            }

        }


        // Restore previous symbols if this rule instance is reused across top-level classes in the same file
        // or when leaving a nested concurrent class context
        staticRandomFieldSymbols.clear();
        staticRandomFieldSymbols.addAll(previousSymbols);
        return super.visit(node, data); // This will trigger visits to methods etc.
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        // Check for Math.random() calls only if we are inside a concurrent context class
        // (We rely on the visit(ASTClassDeclaration) having set up the context)
        if (isInConcurrentContext(node)) { // Helper to check ancestry
            if ("random".equals(node.getMethodName()) && node.getQualifier() != null) {
                // Check if the qualifier is java.lang.Math
                // Using TypeTestUtil.isExactlyA for precise matching
                if (TypeTestUtil.isExactlyA(Math.class, node.getQualifier())) {
                    addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".violation.msg.math.random");
                }
            }
        }
        // Continue traversal for nested calls etc.
        return super.visit(node, data);
    }


    @Override
    public Object visit(ASTVariableAccess node, Object data) {
        // Check for usage of static Random fields identified earlier,
        // but only if inside a relevant concurrent class context.
        if (!staticRandomFieldSymbols.isEmpty() && isInConcurrentContext(node)) {
            JVariableSymbol accessedSymbol = node.getReferencedSym();
            if (accessedSymbol != null && staticRandomFieldSymbols.contains(accessedSymbol)) {

            }
        }
        // Continue traversal
        return super.visit(node, data);
    }

    /**
     * Checks if the given class declaration extends Thread or implements Runnable.
     */
    private boolean isConcurrentContext(ASTClassDeclaration node) {
        // Check inheritance (extends Thread)
        ASTExtendsList extendsList = node.firstChild(ASTExtendsList.class);
        if (extendsList != null) {
            for (ASTClassType astClassType : extendsList) {
                if (TypeTestUtil.isA(Thread.class, astClassType)) { // PMD 7 uses TypeNode get(0)
                    return true;
                }
            }

        }

        // Check implementation (implements Runnable)
        ASTImplementsList implementsList = node.firstChild(ASTImplementsList.class);
        if (implementsList != null) {
            for (ASTClassType astClassType : implementsList) {
                if (TypeTestUtil.isA(Runnable.class, astClassType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the current node is within a class determined to be a concurrent context.
     * This relies on the fact that we only process children *after* identifying
     * a class as a concurrent context in visit(ASTClassDeclaration).
     * A more robust way might involve checking ancestors, but this works if the
     * rule logic flows correctly from the class declaration downwards.
     * We check if the closest ClassDeclaration ancestor is a concurrent context.
     */
    private boolean isInConcurrentContext(JavaNode node) {
        ASTClassDeclaration enclosingClass = node.ancestors(ASTClassDeclaration.class).first();
        return enclosingClass != null && isConcurrentContext(enclosingClass);
        // Note: This simple check might need refinement if dealing with complex nested
        // anonymous classes where the immediate enclosing class isn't the one extending Thread/Runnable,
        // but it covers the common cases.
    }
}
