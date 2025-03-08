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

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // PMD 7.0中获取文件名的方式发生了变化
        RuleContext ruleContext = (RuleContext) data;
        String sourceCodeFilename = "";

        // 尝试从RuleContext获取文件名
        try {
            // 使用反射尝试不同方法获取文件名
            if (ruleContext.getClass().getMethod("getSourceCodeFilename") != null) {
                sourceCodeFilename = (String) ruleContext.getClass().getMethod("getSourceCodeFilename").invoke(ruleContext);
            } else {
                // 尝试其他可能的路径
                Object report = ruleContext.getReport();
                if (report != null) {
                    Object file = report.getClass().getMethod("getFile").invoke(report);
                    if (file != null) {
                        sourceCodeFilename = (String) file.getClass().getMethod("getName").invoke(file);
                    }
                }
            }
        } catch (Exception e) {
            // 如果无法获取文件名，使用默认空名称
            sourceCodeFilename = EMPTY_FILE_NAME;
        }

        // Do type resolve if file name is empty(unit tests).
        if (StringUtils.isBlank(sourceCodeFilename) || EMPTY_FILE_NAME.equals(sourceCodeFilename)) {
            resolveType(node, data);
            return super.visit(node, data);
        }

        // If file name is not empty, use filename + hashcode to identify a compilation unit.
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

    private void resolveType(ASTCompilationUnit node, Object data) {
        // PMD 7.0类型解析系统完全改变
        try {
            // 创建类型解析器
            FixClassTypeResolver classTypeResolver = new FixClassTypeResolver(AbstractAliRule.class.getClassLoader());

            // 尝试使用反射找到正确的类型解析方法
            try {
                // 方法一: 尝试直接设置类型解析器
                java.lang.reflect.Method setTypeResolverMethod = node.getClass().getMethod("setTypeResolutionFacade", Object.class);
                if (setTypeResolverMethod != null) {
                    setTypeResolverMethod.invoke(node, classTypeResolver);
                }
            } catch (NoSuchMethodException e1) {
                try {
                    // 方法二: 尝试使用符号表
                    Object symbolTable = node.getClass().getMethod("getSymbolTable").invoke(node);
                    if (symbolTable != null) {
                        // 尝试其他可能的方法名
                        try {
                            symbolTable.getClass().getMethod("resolveTypes", classTypeResolver.getClass()).invoke(symbolTable, classTypeResolver);
                        } catch (NoSuchMethodException e2) {
                            // 降级到访问者模式
                            node.jjtAccept(classTypeResolver, data);
                        }
                    }
                } catch (Exception e3) {
                    // 最后尝试直接使用访问者模式
                    node.jjtAccept(classTypeResolver, data);
                }
            }
        } catch (Exception e) {
            System.err.println("Type resolution failed: " + e.getMessage());
        }
    }
}