PMD如何调用p3c-pmd规则类的流程
PMD调用p3c-pmd项目中规则类的完整链条如下：


1. 规则加载阶段
   PMD启动并读取规则集XML文件（如rulesets/java/ali-set.xml）
   解析XML中的规则定义：
   <rule name="ConcurrentExceptionWithModifyOriginSubListRule"
   class="com.alibaba.p3c.pmd.lang.java.rule.set.ConcurrentExceptionWithModifyOriginSubListRule">
   通过Java反射机制实例化规则类
2. 代码解析阶段
   PMD解析Java源文件生成抽象语法树(AST)
   创建RuleContext对象，包含源代码文件名等信息
   针对AST根节点ASTCompilationUnit开始规则检查
3. 规则执行阶段
   通过继承链调用规则方法：


AbstractJavaRule (PMD核心) - 提供基础规则框架
AbstractAliRule - 增强了类型解析和国际化支持
@Override
public Object visit(ASTCompilationUnit node, Object data) {
// 进行类型解析增强
resolveType(node, data);
return super.visit(node, data);
}
具体规则类 ConcurrentExceptionWithModifyOriginSubListRule
@Override
public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
// 实现具体检查逻辑
// 使用XPath查询找到subList调用
List<Node> nodes = node.findChildNodesWithXPath(XPATH);
// 检查是否违反规则
}
4. 违规报告阶段
   规则检测到违规时调用：
   addViolationWithMessage(data, blockItem,
   "java.set.ConcurrentExceptionWithModifyOriginSubListRule.violation.msg",
   new Object[] {blockItem.getImage()});
   AbstractAliRule 中增强的消息处理添加国际化支持
   PMD收集所有违规并生成最终报告
   测试框架
   测试类ExtendRuleTst和ExtendSimpleAggregatorTst扩展了PMD的测试框架，用于验证规则功能：

---------------------------------------------------------------------------------------------------------------------------------------------------------------------
visit 方法和 addViolation 方法在 PMD 规则中紧密合作，共同完成代码规则检查和违规报告的任务。 它们之间的关系可以概括为：

visit 方法负责检测违规，addViolation 方法负责报告违规。

更详细地解释它们的合作方式：

visit 方法遍历 AST 并检测规则条件：

当 PMD 规则引擎开始执行规则时，它会从 AST 的根节点开始，通过调用 visit 方法递归地遍历整个抽象语法树 (AST)。
每个 visit 方法都对应于一种特定的 AST 节点类型（例如，ASTMethodDeclaration for method declarations, ASTIfStatement for if statements）。
在 visit 方法内部，规则编写者会编写代码来 检查当前访问的 AST 节点是否满足规则定义的违规条件。 这通常涉及到访问节点的属性、检查节点的结构、或者进行一些逻辑判断。
visit 方法中发现违规时调用 addViolation：

如果在 visit 方法的检查过程中，检测到当前 AST 节点违反了规则，那么 visit 方法就会 调用 addViolation 方法 来报告这个违规。
addViolation 方法的作用是 记录违规信息，包括：
违规节点 (Node): 指出 AST 中哪个节点违反了规则。
违规消息 (Message): 描述违规的具体内容，通常会提供给用户查看。
规则上下文数据 (Data - older PMD versions or implicitly via RuleContext): 一些额外的上下文信息，用于更详细地描述违规 (在新的 PMD 版本中，更多使用 RuleContext 来传递上下文)。
addViolation 方法记录违规信息到 RuleContext：

addViolation 方法本身 并不直接处理违规报告的输出或展示。 它的主要职责是将违规信息 添加到 RuleContext 对象 中。
RuleContext 可以看作是一个 容器，用于 收集在规则执行过程中发现的所有违规信息。 每个规则执行上下文 (例如，检查一个文件) 都会有一个对应的 RuleContext 实例。
规则引擎在遍历结束后处理 RuleContext 中的违规信息：

当 PMD 规则引擎完成对整个 AST 的遍历后，它会 检查 RuleContext 对象，获取其中记录的所有违规信息。
PMD 引擎会根据配置的报告格式 (例如，文本、XML、HTML 等)，将 RuleContext 中收集到的违规信息 输出到报告文件或控制台，最终呈现给用户。
用一个简单的例子来说明：

假设你有一个 PMD 规则，用于检查方法名是否过长 (例如，超过 30 个字符就认为违规)。

visit(ASTMethodDeclaration node, RuleContext ruleContext) 方法的实现可能如下：

Java

public Object visit(ASTMethodDeclaration node, RuleContext ruleContext) {
String methodName = node.getMethodName(); // 获取方法名
if (methodName.length() > 30) { // 检查方法名长度是否超过 30
String message = "方法名 '" + methodName + "' 过长，建议缩短到 30 个字符以内。";
ruleContext.addViolationWithMessage(node, message); // 调用 addViolationWithMessage 报告违规
}
super.visit(node, ruleContext); // 继续遍历子节点
return data;
}
在这个例子中：

visit(ASTMethodDeclaration node, RuleContext ruleContext) 方法负责 遍历到每个方法声明节点 (ASTMethodDeclaration)，并 获取方法名。
if (methodName.length() > 30) 条件判断 方法名是否过长，如果过长，则认为违反了规则。
ruleContext.addViolationWithMessage(node, message) 在检测到违规时被调用，将违规节点 (node) 和违规消息 (message) 添加到 ruleContext 中。
最终的报告：  当 PMD 引擎遍历完所有代码文件后，会根据 RuleContext 中收集到的违规信息，生成报告，用户就可以看到类似这样的违规提示：

[文件: MyClass.java, 行号: 10] 方法名 'aVeryLongMethodNameThatExceedsThirtyCharacters' 过长，建议缩短到 30 个字符以内。 (LongMethodNameRule)
总结 visit 和 addViolation 的合作：

功能组件	职责	工作时机	目的
visit 方法	检测代码是否违反规则	PMD 规则引擎遍历 AST 时，访问到特定节点类型时	发现代码中潜在的规则违规
addViolation 方法	报告检测到的违规	在 visit 方法中检测到违规后	将违规信息记录到 RuleContext，以便后续生成报告

Export to Sheets
核心要点：

visit 方法是 主动的，由 PMD 引擎驱动，遍历 AST 并执行规则逻辑。
addViolation 方法是 被动的，由 visit 方法在检测到违规时 主动调用，用于报告违规。
RuleContext 是 桥梁，连接 visit 方法和最终的违规报告。 visit 方法通过 addViolation 将违规信息放入 RuleContext，PMD 引擎从 RuleContext 中提取信息生成报告。
理解 visit 和 addViolation 的协作关系，有助于你更好地编写 PMD 规则，有效地检测和报告代码中的问题。



---------------------------------------------------------------------------------------------------------------------------------------------------------------------
AbstractAliRule 和 FixClassTypeResolver 修改的逻辑和思路：

PMD 6.x 与 PMD 7.0 的架构差异及代码修改思路解析
1. PMD 6.x 与 PMD 7.0 的核心架构差异
   PMD 6.x 架构特点
   访问者模式实现：使用 jjtAccept 进行节点访问
   类型解析：通过 ClassTypeResolver 类解析类型信息
   违规报告：使用 RuleContext 的 getReport() 方法获取报告对象
   节点遍历：节点树通过显式遍历处理
   PMD 7.0 架构特点
   访问者模式重构：使用访问者接口 (JavaVisitor)，移除 jjtAccept 方法
   引入 AstInfo：新增 AstInfo 类管理语法树元数据
   新型符号表：添加 JSymbolTable 和 TypeSystem 进行类型管理
   报告机制变更：通过 RuleContext 直接添加违规信息
   Java 21 支持：支持 Java 21 的未命名类等新特性
2. AbstractAliRule 修改思路解析
   原 AbstractAliRule 类中的主要问题：


使用了已弃用的 jjtAccept 方法进行遍历
尝试访问不再存在的 Report 对象
无法正确获取文件名
修改思路
文件名获取：


String sourceCodeFilename = node.getReportLocation().getFileId().getFileName();
在 PMD 7.0 中，文件名不再通过 Report 获取，而是通过 ReportLocation 对象


类型解析：


private void resolveType(ASTCompilationUnit node, Object data) {
// 首先尝试使用 AstInfo 中的符号表
Object astInfo = node.getAstInfo();
if (astInfo != null) {
// 通过反射调用符号表解析
} else {
// 降级使用自定义解析器
}
}
使用分层策略，优先使用 PMD 7.0 内置符号表，失败时降级至自定义解析


使用反射：


Object symbolTable = astInfo.getClass().getMethod("getSymbolTable").invoke(astInfo);
通过反射实现更松散的耦合，提高代码对不同 PMD 版本的适应性


3. FixClassTypeResolver 修改思路解析
   原 FixClassTypeResolver 类无法在 PMD 7.0 中使用，因为：


继承的 ClassTypeResolver 类已不存在
依赖的 jjtAccept 方法已移除
类型解析机制完全变更
修改思路
移除继承关系：


public class FixClassTypeResolver { // 不再继承 ClassTypeResolver
由于父类在 PMD 7.0 中已经不存在


直接使用新 API：


JSymbolTable symbolTable = node.getSymbolTable();
TypeSystem typeSystem = node.getTypeSystem();
直接调用 PMD 7.0 提供的类型系统 API


反射机制适配：


Method resolveMethod = getResolveMethod(symbolTable.getClass());
if (resolveMethod != null) {
resolveMethod.invoke(symbolTable);
}
使用反射查找并调用解析方法，增强兼容性


错误处理与日志：


LOG.log(Level.WARNING, "Type resolution failed: " + e.getMessage(), e);
加强错误处理，便于问题定位


4. 技术实现的深层考虑
   向后兼容性：使用反射和防御性编程，确保代码在 PMD 7.0 各次要版本间保持兼容


隔离变化：将 PMD 版本变化隔离在 FixClassTypeResolver 中，减少对业务规则的影响


性能考量：维持类型信息缓存机制 (TYPE_RESOLVER_MAP)，避免重复解析


错误恢复：即使类型解析失败，规则检查仍然可以继续执行


5. 修改的根本原因
   PMD 7.0 相比 6.x 进行了深度重构，几乎整个核心架构都发生了变化。我们的修改是为了：


适应新架构：符合 PMD 7.0 的设计理念和 API
保持功能等价：确保规则检查质量不受影响
提供向后兼容：确保各种 PMD 7.x 版本都可以正常工作
维持代码结构：尽量保留原有代码组织和命名，减少学习成本
通过这些修改，我们成功地将阿里巴巴 P3C 代码规范检查工具迁移到了 PMD 7.0 平台，同时保持了规则的有效性和代码的可维护性。
---------------------------------------------------------------------------------------------------------------------------------------------------------------------


此代码通过四个关键步骤工作：


动态查找方法：astInfo.getClass().getMethod("getSymbolTable") 通过反射查找 AstInfo 类的 getSymbolTable 方法


调用获取符号表方法：.invoke(astInfo) 在 astInfo 实例上调用上一步找到的方法，获取符号表对象


查找解析方法：symbolTable.getClass().getMethod("resolve") 查找符号表类的 resolve 方法


触发符号表解析：.invoke(symbolTable) 调用解析方法，激活符号表的类型解析机制


为什么使用反射实现
使用反射的主要原因有：


兼容性考虑：避免直接依赖特定版本的 PMD API，增强兼容性


PMD 7.0 是大版本更新，API 可能在小版本间有所变动
通过反射可以在不重新编译的情况下适应这些变化
防御性编程：使用 try-catch 包装整个过程，确保即使符号表获取失败也不会中断规则检查


在类型解析失败时提供清晰的错误信息
允许降级到备选实现
解耦合设计：减少对 PMD 内部实现的直接依赖


PMD 的内部 API 可能会改变，但反射机制让代码更加稳定
避免直接类型引用，降低编译时依赖
与 PMD 7.0 架构变化的关系
PMD 7.0 引入的 AstInfo 和 JSymbolTable 表示语法树元数据和符号信息，这段代码正是为了适应这一架构变化。在 PMD 6.x 中，类型解析通常通过 ClassTypeResolver 完成，而 PMD 7.0 将这一职责移交给了符号表系统。

通过反射触发符号表的解析机制，这段代码在不直接依赖新 API 的情况下实现了与 PMD 7.0 类型系统的对接，为规则检查提供了必要的类型信息。

PMD 工作流程中的位置
在 PMD 的规则检查流程中，这段代码处于以下位置：
源代码解析 → 生成 AST
类型解析 ← 我们讨论的代码在这里
规则检查 → 识别违规
生成报告 → 输出结果


这段代码在 PMD 工作原理中完成了以下核心任务：


符号表延迟加载触发：PMD 7.0 使用懒加载方式处理符号表，调用 resolve() 方法触发真正的符号解析


类型信息构建：激活符号表后，PMD 会建立：


变量定义和引用之间的映射
方法调用和定义之间的��接
类型继承和实现关系���解析
作用域信息：确定变量的可见性和有效范围，这对于许多规则检查至关重要


对规则检查的重要性
这段代码对规则检查至关重要，因为：


类型依赖规则：许多规则需要类型信息才能正常工作，如：


接口与实现检查规则
类型转换安全性规则
泛型使用规则
引用关系：确定变量、方法引用的正确性，是检查未使用代码、冗余代码的基础


准确性保障：如果没有这一��，许多依赖类型信息的规则将无法正常工作或产生误报


在 PMD 6.x 中，这一步是通过 ClassTypeResolver 自动完成的，而在 PMD 7.0 中需要我们主动触发符号表的解析过程。这段反射代码正是确保了这一关键步骤在新架构下仍然能够正确执行。

