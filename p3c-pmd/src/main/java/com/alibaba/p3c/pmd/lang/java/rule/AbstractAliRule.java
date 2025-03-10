package com.alibaba.p3c.pmd.lang.java.rule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.fix.FixClassTypeResolver;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.reporting.RuleContext;
import org.apache.commons.lang3.StringUtils;

/**
 * re calculate node type
 *
 */
public abstract class AbstractAliRule extends AbstractJavaRule {

    private static final Map<String, Boolean> TYPE_RESOLVER_MAP = new ConcurrentHashMap<>(16);

    private static final String EMPTY_FILE_NAME = "n/a";
    private static final String DELIMITER = "-";

    /**
     * 重写visit方法，解决PMD 7.0中类型解析问题
     * ASTCompilationUnit 的作用
     * ASTCompilationUnit 是 Java 源代码文件在 PMD 中的抽象语法树(AST)根节点，代表整个源文件结构：
     *
     *
     * OrdinaryCompilationUnit：常规 Java 文件，包含包声明、导入和类型声明
     * UnnamedClassCompilationUnit：Java 21 预览特性，支持未命名类和实例方法
     * ModularCompilationUnit：模块声明文件 (module-info.java)
     * 代码检查流程
     * PMD 解析 Java 源码生成 ASTCompilationUnit
     * 调用 visit(ASTCompilationUnit node, Object data) 方法遍历 AST
     * 规则检查逻辑识别违规代码
     * 通过 addViolationWithMessage() 记录违规
     *
     * @param node 这是所有AST类型的根节点
     * @param data
     * @return
     */
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        RuleContext ruleContext = (RuleContext) data;

        // PMD 7.0 获取文件名的标准方式
        String sourceCodeFilename = node.getReportLocation().getFileId().getFileName();

        // 类型解析逻辑...
        if (StringUtils.isBlank(sourceCodeFilename)) {
            resolveType(node, data);
            return super.visit(node, data);
        }

        // 使用文件名+哈希码识别编译单元
        String uniqueId = sourceCodeFilename + DELIMITER + node.hashCode();
        if (!TYPE_RESOLVER_MAP.containsKey(uniqueId)) {
            resolveType(node, data);
            TYPE_RESOLVER_MAP.put(uniqueId, true);
        }
        return super.visit(node, data);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessage(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }

    public void addViolationWithMessage(Object data, Node node, String message) {
        ((RuleContext)data).addViolationWithMessage(node, I18nResources.getMessageWithExceptionHandled(message));
    }

    public void addViolationWithMessage(Object data, Node node, String message, Object[] args) {
        ((RuleContext)data).addViolationWithMessage(node, String.format(I18nResources.getMessageWithExceptionHandled(message), args));
    }

    private void resolveType(ASTCompilationUnit astNode, Object data) {
        try {
            // 创建类型解析器
            FixClassTypeResolver classTypeResolver = new FixClassTypeResolver(AbstractAliRule.class.getClassLoader());

            // 在 PMD 7.0 中获取 AstInfo
            Object astInfo = astNode.getAstInfo();
            if (astInfo != null) {
                // 尝试使用 AstInfo 中的符号表
                try {
                    Object symbolTable = astInfo.getClass().getMethod("getSymbolTable").invoke(astInfo);
                    if (symbolTable != null) {
                        symbolTable.getClass().getMethod("resolve").invoke(symbolTable);
                    }
                } catch (Exception e) {
                    // 符号表访问失败，尝试其他方法
                    System.err.println("Symbol table access failed: " + e.getMessage());
                }
            } else {
                // 降级到使用自定义解析器
                // 注意：在 PMD 7.0 中可能需要其他技术来实现类型解析
                classTypeResolver.begin(astNode);
            }
        } catch (Exception e) {
            System.err.println("Type resolution failed: " + e.getMessage());
        }
    }}