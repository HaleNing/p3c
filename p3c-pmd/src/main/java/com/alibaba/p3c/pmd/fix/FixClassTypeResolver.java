package com.alibaba.p3c.pmd.fix;

import net.sourceforge.pmd.lang.ast.AstInfo;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;
import net.sourceforge.pmd.lang.java.types.TypeSystem;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PMD 7.0 适配的类型解析器
 * 在 PMD 7.0 中，类型解析 SymbolTable 和 TypeSystem 处理
 * 此类不再继承 ClassTypeResolver (已在 PMD 7.0 移除)
 */
public class FixClassTypeResolver {

    private static final Logger LOG = Logger.getLogger(FixClassTypeResolver.class.getName());
    private final ClassLoader classLoader;
    private final Map<String, Class<?>> resolvedTypes = new HashMap<>();

    public FixClassTypeResolver(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getClass().getClassLoader();
    }

    /**
     * 开始类型解析过程
     * 在 PMD 7.0 中，我们需要通过 AstInfo 访问符号表
     *
     * @param node AST编译单元
     */
    public void begin(ASTCompilationUnit node) {
        try {
            if (node == null) {
                LOG.log(Level.WARNING, "Cannot resolve types for null compilation unit");
                return;
            }

            // 获取 AstInfo
            AstInfo<ASTCompilationUnit> astInfo = node.getAstInfo();
            if (astInfo == null) {
                LOG.log(Level.WARNING, "AstInfo is null for compilation unit");
                return;
            }

            // 获取符号表
            JSymbolTable symbolTable = getSymbolTable(node);
            if (symbolTable != null) {
                // 触发符号表解析
                resolveSymbolTable(symbolTable);
            }

            // 获取类型系统
            TypeSystem typeSystem = node.getTypeSystem();
            if (typeSystem == null) {
                LOG.log(Level.WARNING, "TypeSystem is null for compilation unit");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Type resolution failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取符号表
     */
    private JSymbolTable getSymbolTable(ASTCompilationUnit node) {
        try {
            return node.getSymbolTable();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get symbol table: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过反射调用符号表的解析方法
     */
    private void resolveSymbolTable(JSymbolTable symbolTable) {
        try {
            // 尝试查找和调用 resolve 方法
            Method resolveMethod = getResolveMethod(symbolTable.getClass());
            if (resolveMethod != null) {
                resolveMethod.invoke(symbolTable);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to resolve symbol table: " + e.getMessage(), e);
        }
    }

    /**
     * 获取符号表的解析方法
     */
    private Method getResolveMethod(Class<?> clazz) {
        try {
            return clazz.getMethod("resolve");
        } catch (NoSuchMethodException e) {
            // PMD 7.0 可能使用不同的方法名，尝试查找其他可能的方法
            for (Method method : clazz.getMethods()) {
                if (method.getName().contains("resolve") && method.getParameterCount() == 0) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * 尝试加载指定类名的类
     */
    public Class<?> loadClass(String fullyQualifiedClassName) {
        if (resolvedTypes.containsKey(fullyQualifiedClassName)) {
            return resolvedTypes.get(fullyQualifiedClassName);
        }

        try {
            Class<?> clazz = classLoader.loadClass(fullyQualifiedClassName);
            resolvedTypes.put(fullyQualifiedClassName, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            resolvedTypes.put(fullyQualifiedClassName, null);
            return null;
        }
    }

    /**
     * 检查类名是否存在
     */
    public boolean classNameExists(String fullyQualifiedClassName) {
        return loadClass(fullyQualifiedClassName) != null;
    }
}