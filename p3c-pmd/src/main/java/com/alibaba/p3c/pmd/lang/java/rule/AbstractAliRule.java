package com.alibaba.p3c.pmd.lang.java.rule;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.fix.FixClassTypeResolver;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;
import net.sourceforge.pmd.reporting.RuleContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractAliRule extends AbstractJavaRule {

    private static final Map<String, Boolean> TYPE_RESOLVER_MAP = new ConcurrentHashMap<>(16);

    private static final String EMPTY_FILE_NAME = "n/a";
    private static final String DELIMITER = "-";

    /**
     *
     * @param compilationUnitNode 这是所有AST类型的根节点
     * @param data
     * @return
     */
    @Override
    public Object visit(ASTCompilationUnit compilationUnitNode, Object data) {
        RuleContext ruleContext = (RuleContext) data;

        // PMD 7.0 获取文件名的标准方式
        String sourceCodeFilename = compilationUnitNode.getReportLocation().getFileId().getFileName();

        // 类型解析逻辑...
        if (StringUtils.isBlank(sourceCodeFilename) || EMPTY_FILE_NAME.equals(sourceCodeFilename)) {
            resolveType(compilationUnitNode, data);
            return super.visit(compilationUnitNode, data);
        }

        // 使用文件名+哈希码识别编译单元
        String uniqueId = sourceCodeFilename + DELIMITER + compilationUnitNode.hashCode();
        if (!TYPE_RESOLVER_MAP.containsKey(uniqueId)) {
            resolveType(compilationUnitNode, data);
            TYPE_RESOLVER_MAP.put(uniqueId, true);
        }
        return super.visit(compilationUnitNode, data);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessage(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }


    public void addViolationWithMessage(Object data, JavaNode node, String message) {
        super.asCtx(data).addViolationWithMessage(node, I18nResources.getMessageWithExceptionHandled(message));
    }

    public void addViolationWithMessage(Object data, JavaNode node, String message, Object[] args) {
        super.asCtx(data).addViolationWithMessage(node, I18nResources.getMessageWithExceptionHandled(message), args);
    }


    /**
     *
     * @param compilationUnitNode
     * @param data
     */
    private void resolveType(ASTCompilationUnit compilationUnitNode, Object data) {
        try {
            // 创建类型解析器
            FixClassTypeResolver classTypeResolver = new FixClassTypeResolver(AbstractAliRule.class.getClassLoader());

            try {
                JSymbolTable symbolTable = compilationUnitNode.getSymbolTable();
                symbolTable.getClass().getMethod("resolve").invoke(symbolTable);
            } catch (Exception e) {
                // 符号表访问失败，尝试其他方法
                System.err.println("Symbol table access failed: " + e.getMessage());
                classTypeResolver.begin(compilationUnitNode);
            }

        } catch (Exception e) {
            System.err.println("Type resolution failed: " + e.getMessage());
        }
    }
}