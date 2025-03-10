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

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
在修改 VariableUtils 类时，我的思路是保持原有功能的同时，使其更好地适配 PMD 7 的 API 和支持不同类型的变量声明节点。以下是我的思路和逻辑：

1. 抽象基类的使用
   我注意到 PMD 7 中的 ASTFieldDeclaration 和 ASTLocalVariableDeclaration 都实现了 JavaNode 接口，并且都支持
   descendants() 方法来查找子节点。在 AvoidPatternCompileInMethodRule 中，代码需要从本地变量声明中获取变量名，但原始的
   VariableUtils 只支持字段声明。

使用 JavaNode 作为基础参数类型，而不是 Object，可以：

确保类型安全
访问公共功能（如 descendants() 方法）
避免不必要的类型转换

2. 方法重载结构
   我设计了一个三层结构：

基础方法接受 JavaNode 参数，包含通用实现逻���
特定类型的重载方法（针对 ASTFieldDeclaration 和 ASTLocalVariableDeclaration）调用基础方法
这样的结构实现了代码复用，同时保持了API的类型安全性

3. PMD 7 流式 API 的利用
   PMD 7 提供了流式 API 来处理 AST 节点：

使用 descendants(Class<T>) 代替旧版的 findDescendantsOfType
使用 first() 方法获取第一个元素
这些 API ���代码更简洁，也更符合现代 Java 的编程风格

4. 保持兼容性与扩展性
   我保留了原有的方法签名，同时添加了新的方法，这样做：

确保现有代��（如 AvoidPatternCompileInMethodRule）不需要修改
为未来可能的扩展提供了基础（如支持更多类型的声明节点）

5. 代码清晰性与一致性
   每个方法都有清晰的 JavaDoc 注释，解释其功能和参数含义
   处理空值的一致性检查
   确保了变量获取逻辑的一致性
   这种设计使 VariableUtils 成为了一个更通用、更灵活的工具类，不仅满足当前的需求，也为未来可能的扩展提供了基础。同时，它充分利用了
   PMD 7 的新特性，使代码更加简洁和高效

---------------------------------------------------------------------------------------------------------------------------------------------------
从语法规则 FieldDeclaration ::= ModifierList Type VariableDeclarator ( "," VariableDeclarator )* ";"
的角度来看，ModifierList、Type 和 VariableDeclarator 可以被认为是 FieldDeclaration 的主要子节点。

让我们更详细地解释一下：

语法规则的结构体现了树形结构： 语法规则 FieldDeclaration ::= ModifierList Type VariableDeclarator ( ","
VariableDeclarator )* ";"  本身就暗示了一种树形结构。 FieldDeclaration 可以看作是父节点，而 ModifierList、Type 和
VariableDeclarator 是构成 FieldDeclaration 的主要组成部分，因此在 AST 中它们会成为 FieldDeclaration 节点的子节点。

AST 的目的就是表示语法结构： 抽象语法树 (AST) 的核心目的是将源代码的语法结构以树形的方式表示出来。 语法规则正是定义语法结构的蓝图，因此
AST 的结构通常会直接反映语法规则的定义。

每个子节点代表 FieldDeclaration 的一个组成部分：

ModifierList: 代表字段声明中的修饰符列表。在 AST 中，FieldDeclaration
节点会有一个子节点来表示解析到的修饰符信息。这个子节点本身可能也是一个列表或集合类型的节点，包含多个修饰符节点（例如
ASTPublic, ASTStatic, ASTFinal 等）。
Type: 代表字段声明的数据类型。在 AST 中，FieldDeclaration 节点会有一个子节点来表示解析到的类型信息。这个子节点会指向表示具体类型的
AST 节点（例如 ASTPrimitiveType 表示基本数据类型，ASTClassOrInterfaceType 表示类或接口类型等）。
VariableDeclarator: 代表变量声明符。 虽然语法规则中 VariableDeclarator 后面跟着 ( "," VariableDeclarator )*，意味着可以有多个
VariableDeclarator，但在 AST 中，通常 FieldDeclaration 节点会包含一个 子节点列表，每个子节点都是一个 VariableDeclarator 节点。
每个 VariableDeclarator 节点又会包含变量名和可能的初始化表达式等信息。
( "," VariableDeclarator )* " 的处理： 语法规则中的 ( "," VariableDeclarator )*  表示可以有零个或多个 ,
VariableDeclarator 组合。 在 AST 中，这通常被处理为 FieldDeclaration 节点下包含一个 VariableDeclarator 节点的列表。
即使只有一个变量声明，这个列表也至少包含一个 VariableDeclarator 节点。 如果有多个变量声明（用逗号分隔），列表中就会包含多个
VariableDeclarator 节点。

分号 ; 不是子节点： 语法规则末尾的分号 ; 是语句的结束符，它在语法结构上是 FieldDeclaration 的一部分，但 在 AST
的树形结构中，分号通常不会被表示为 FieldDeclaration 节点的子节点。 AST 主要关注代码的抽象语法结构，而像分号这样的标点符号，在语法分析阶段主要起分隔和标记语句结束的作用，在
AST 中通常会被省略，或者作为节点属性信息来处理，而不是作为独立的子节点。

总结来说：

在 PMD 7 的 AST 中，当你遇到一个 ASTFieldDeclaration 节点时，你可以预期它会拥有以下主要子节点（或者子节点列表）：

一个表示 ModifierList 的子节点 (或子节点列表)： 例如，可能是一个 ASTModifiers 类型的节点，它本身可能包含 ASTPublic,
ASTStatic, ASTFinal 等修饰符节点。
一个表示 Type 的子节点: 例如，可能是 ASTPrimitiveType, ASTClassOrInterfaceType, ASTArrayType 等类型的节点，具体取决于字段的数据类型。
一个或多个表示 VariableDeclarator 的子节点: 例如，ASTVariableDeclarator 类型的节点，每个节点代表一个变量声明，并包含变量名和可能的初始化表达式等信息。
因此，是的，ModifierList、Type 和 VariableDeclarator 可以被认为是 FieldDeclaration 的主要子节点，它们共同构成了
FieldDeclaration 在 AST 中的表示。 理解这种父子节点关系对于遍历和分析 PMD 的 AST 非常重要。
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
ASTCompilationUnit 和 JavaNode 都是 PMD 中用于表示抽象语法树 (AST) 的概念，但它们之间存在关键的区别，主要体现在
作用域、层级和具体含义 上。

我们可以用一个比喻来帮助理解：如果把整个 Java 源代码文件比作一棵树，那么 ASTCompilationUnit 就是这棵树的根节点，而 JavaNode
则可以看作是树中所有节点的通用类型或基类。

下面我们从几个方面详细解释它们之间的区别：

1. 作用域和层级：

ASTCompilationUnit (编译单元抽象语法树节点)：

作用域： 代表 整个 Java 源代码文件 (一个 .java 文件)。
层级： 是 AST 的根节点，位于整个 AST 树的顶层。
含义： 它封装了整个编译单元的抽象语法结构，是访问和遍历整个 AST 的入口点。一个 Java 源文件只有一个 ASTCompilationUnit 节点。
JavaNode (Java 节点)：

作用域： 代表 AST 中的任何节点，包括语句、表达式、声明、类型等等。
层级： 是一个 更通用的概念或基类，位于 AST 节点层级结构的较底层。 ASTCompilationUnit 本身也是一种 JavaNode，但 JavaNode
包括了更多种类的节点。
含义： JavaNode 定义了所有 AST 节点 通用的属性和行为，例如：
代码位置信息 (行号、列号等)。
父节点和子节点的访问方法。
接受访问者 (Visitor) 的方法 (用于实现 PMD 的规则检查机制)。

2. 具体含义和功能：

ASTCompilationUnit：

代表一个完整的编译单元： 它对应于一个 .java 文件，包含了包声明、导入语句、类型声明（类、接口、枚举、注解）等顶层结构。
作为 AST 的入口点： PMD 的代码分析通常从 ASTCompilationUnit 节点开始，遍历其子节点，检查代码是否符合规则。
提供了访问顶层结构的方法： 例如，通过 ASTCompilationUnit 可以访问到文件中的所有类型声明 (类、接口等)。
JavaNode：

是一个抽象概念或接口： JavaNode 更像是一个接口或抽象基类，定义了所有 AST 节点应该具备的通用特性。
是所有具体 AST 节点的父类： PMD 中各种具体的 AST 节点类型（例如
ASTClassDeclaration、ASTMethodDeclaration、ASTFieldDeclaration、ASTIfStatement、ASTExpression 等等）都 继承自或实现了
JavaNode 的接口。
提供了访问 AST 结构的基本方法： 例如，jjtGetParent(), jjtGetChild(), jjtGetNumChildren() 等方法是 JavaNode 提供的通用方法，用于在
AST 树中导航。
支持 Visitor 模式： JavaNode 接口通常会定义 jjtAccept(JavaParserVisitor visitor, Object data) 方法，用于支持访问者模式，这是
PMD 规则检查机制的核心。

3. 类比总结：

特性 ASTCompilationUnit JavaNode
作用域 整个 Java 源代码文件 (.java 文件)    AST 中的任何节点
层级 AST 的根节点 AST 节点层级结构的基类或通用类型
具体性 具体，代表编译单元 抽象，通用节点类型
功能 AST 入口点，访问顶层结构 定义 AST 节点通用属性和行为
关系 是一种 JavaNode 是所有具体 AST 节点的基类
比喻 树的根节点 树中的任何一个节点

Export to Sheets
举例说明：

假设我们有以下简单的 Java 代码：

Java

public class MyClass {
private int count;

    public void increment() {
        count++;
    }

}
当 PMD 解析这段代码时，会生成一个 AST。 在这个 AST 中：

ASTCompilationUnit 节点 是整个 AST 的根节点，代表了 MyClass.java 文件。
ASTClassDeclaration 节点 是 ASTCompilationUnit 的子节点，代表了 public class MyClass { ... } 这个类声明。
ASTFieldDeclaration 节点 是 ASTClassDeclaration 的子节点，代表了 private int count; 这个字段声明。
ASTMethodDeclaration 节点 是 ASTClassDeclaration 的子节点，代表了 public void increment() { ... } 这个方法声明。
ASTStatement 节点 (例如 ASTBlock, ASTExpressionStatement) 会是 ASTMethodDeclaration 的子节点，代表方法体内的语句。
ASTName 节点 会出现在各个地方，例如表示类名 MyClass、字段名 count、方法名 increment 等。
所有这些 ASTClassDeclaration, ASTFieldDeclaration, ASTMethodDeclaration, ASTStatement, ASTName 节点，以及
ASTCompilationUnit 节点本身，都是 JavaNode 的子类型或实现。 JavaNode 提供了一套通用的接口和方法，使得可以统一地操作和处理
AST 中的各种节点。

总结：

ASTCompilationUnit 是一个 特定类型的 JavaNode，它代表了 整个 Java 源代码文件的 AST 根节点。 JavaNode 则是一个 更通用的概念，是
所有 AST 节点的基类或接口，定义了 AST 节点共有的特性。 理解它们的区别有助于你更好地理解 PMD 的 AST 结构以及 PMD 如何进行代码分析。


---------------------------------------------------------------------------------------------------------------------------------------------------------------------


理解 JavaNode 和 Node 之间的区别，需要更深入地了解 PMD 的架构和抽象语法树 (AST) 的设计。 实际上，在 PMD 7 中，JavaNode
本身就可以被认为是 Node 的一种特化或者扩展。

为了清晰地解释，我们可以从以下几个方面来理解它们之间的关系和区别：

1. Node 是更通用的接口或抽象基类：

Node (节点)： 在 PMD 的 AST 框架中，Node 通常是一个 最基础、最通用的接口或抽象类。 它可以被设计用来 表示 AST
中所有类型的节点，而不仅仅局限于 Java 语言。
语言无关性 (可能的设计意图)： Node 的设计目标可能是 语言无关的。 这意味着，PMD 框架的设计者可能希望 PMD 不仅能解析 Java
代码，还能解析其他语言（虽然 PMD 核心主要关注 Java）。 因此，Node 接口或基类可能会定义 所有 AST 节点都通用的属性和行为，例如：
父节点和子节点的管理 (例如，获取父节点、添加子节点、获取子节点列表等)。
代码位置信息 (行号、列号等)。
接受访问者 (Visitor) 的方法 (用于实现通用的 AST 遍历和处理机制)。
节点类型信息 (例如，节点代表的是语句、表达式、声明等)。

2. JavaNode 是 Node 的 Java 语言特化：

JavaNode (Java 节点)： JavaNode 可以被认为是 Node 接口或基类在 Java 语言领域的具体实现或扩展。 它 继承或实现了 Node
的通用接口，并且 添加了 Java 语言特有的属性和方法。
Java 语言特性： JavaNode 可能会定义一些 与 Java 语法结构更紧密相关的接口或方法，例如：
访问 Java 特定的语法元素的方法： 例如，获取 Java 节点的修饰符 (modifiers)，获取 Java 节点的类型 (type)，获取 Java
节点的名称 (name) 等等。
可能包含 Java 特定的属性： 例如，与 Java 泛型、注解、模块化等特性相关的属性。

3. 类比理解：

我们可以用 “交通工具” 和 “汽车” 来类比 Node 和 JavaNode：

Node 就像 “交通工具”:  “交通工具” 是一个非常广泛的概念，可以指代任何用于运输的工具，例如汽车、火车、飞机、轮船、自行车等等。
“交通工具” 接口或基类可能会定义所有交通工具都通用的属性和行为，例如 “移动”、“载客/载货”、“燃料类型” 等。

JavaNode 就像 “汽车”:  “汽车” 是 “交通工具” 的一种具体类型，它继承了 “交通工具” 的通用特性（例如 “移动”、“载客/载货”），并且添加了
“汽车” 特有的属性和行为，例如 “引擎类型”、“车轮数量”、“方向盘”、“刹车系统” 等等。

4. 在 PMD 7 中的实际情况 (推测)：

在 PMD 7 的具体实现中，Node 和 JavaNode 的关系可能体现为以下几种情况 (具体需要查看 PMD 7 的源代码才能确定)：

Node 是接口，JavaNode 是实现接口的抽象类： Node 定义了所有 AST 节点必须实现的通用方法，而 JavaNode 作为一个抽象类，实现了
Node 接口，并提供了一些 Java 语言通用的基础实现，然后更具体的 Java AST 节点类型（如 ASTClassDeclaration）再继承自 JavaNode。

Node 是基类，JavaNode 是 Node 的子类： Node 作为基类定义了通用 AST 节点的特性，JavaNode 继承自 Node，并添加 Java
语言特有的功能。更具体的 Java AST 节点类型再进一步继承自 JavaNode。

可能 Node 就是 JavaNode 的别名或早期版本名称： 在某些情况下，可能最初设计时只考虑 Java 语言，使用了 Node 这个名字，后来为了更清晰地表达其
Java 语言的特性，将其重命名为 JavaNode，或者引入 JavaNode 作为更明确的 Java 节点类型，而 Node 可能仍然保留，作为更底层的通用接口或基类。

总结：

Node 是更通用、更抽象的概念，可能被设计为语言无关的 AST 节点基类型，定义了所有 AST 节点通用的特性。
JavaNode 是 Node 在 Java 语言领域的特化和扩展，它继承或实现了 Node 的通用接口，并添加了 Java 语言特有的属性和方法，是所有
Java 特定的 AST 节点的父类。
理解 Node 和 JavaNode 的区别，有助于你从更宏观的角度理解 PMD 的 AST 框架设计。 Node 提供了通用的 AST 节点抽象，而 JavaNode
则专注于 Java 语言的语法结构表示，使得 PMD 能够有效地解析和分析 Java 代码。 更具体的 AST 节点类型（例如 ASTClassDeclaration
等）则是在 JavaNode 的基础上进一步细化，代表了 Java 语言中各种具体的语法元素。



------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
TypeNode 和 JavaNode 都是 PMD 中用于表示抽象语法树 (AST) 节点的概念，但它们代表了 不同的抽象层次和关注点。 简单来说，TypeNode
是 JavaNode 的一个子集，专门用于表示 Java 代码中的类型 (Type)。

我们可以从以下几个方面来理解它们之间的区别：

1. 作用域和抽象层次:

JavaNode (Java 节点):

更通用 (General): JavaNode 是一个 非常通用的接口或抽象基类，它是 PMD 中 所有代表 Java 语法元素的 AST 节点的根类型。
代表任何 Java 语法结构: JavaNode 可以用来表示 任何类型的 Java 语法结构，包括：
声明 (Declarations): 类声明 (ASTClassDeclaration)、接口声明 (ASTInterfaceDeclaration)、方法声明 (ASTMethodDeclaration)
、字段声明 (ASTFieldDeclaration)、变量声明 (ASTVariableDeclaratorId) 等。
语句 (Statements): if 语句 (ASTIfStatement)、for 循环 (ASTForStatement)、while 循环 (ASTWhileStatement)、try-catch 语句 (
ASTTryStatement)、表达式语句 (ASTStatementExpression) 等。
表达式 (Expressions): 方法调用表达式 (ASTPrimaryExpression)、赋值表达式 (ASTAssignmentOperator)、算术表达式 (
ASTAdditiveExpression)、字面量 (ASTLiteral)、变量引用 (ASTName) 等。
类型 (Types): 基本数据类型 (ASTPrimitiveType)、类/接口类型 (ASTClassOrInterfaceType)、数组类型 (ASTArrayType) 等。
修饰符 (Modifiers): public, private, static, final 等修饰符 (ASTModifiers).
编译单元 (Compilation Unit): ASTCompilationUnit (代表整个 Java 源文件)。
TypeNode (类型节点):

更具体 (Specific): TypeNode 是一个 更具体的接口或抽象类，它是 JavaNode 的子类型。
专门用于表示 Java 类型: TypeNode 只用于表示 Java 代码中出现的各种数据类型。 它 不包括语句、表达式、声明等其他语法结构。
TypeNode 的子类型: TypeNode 本身通常也有子类型，用于表示不同种类的 Java 类型，例如：
ASTPrimitiveType: 代表基本数据类型 (如 int, boolean, char 等)。
ASTClassOrInterfaceType: 代表类或接口类型 (如 String, List, MyClass 等)。
ASTArrayType: 代表数组类型 (如 int[], String[][] 等)。
ASTReferenceType: 代表引用类型 (可以是类类型、接口类型或数组类型)。
ASTTypeArguments, ASTTypeParameters, ASTWildCardBounds: 用于表示泛型类型相关的更复杂的类型结构。

2. 关注点和功能:

JavaNode:  关注的是 Java 语法结构的整体表示。 JavaNode 提供了 通用的接口和方法，用于在 AST 树中导航、访问节点信息、支持
Visitor 模式等。 任何你想要在 AST 中表示的 Java 语法元素，都需要使用 JavaNode 或其子类型来表示。

TypeNode:  关注的是 Java 代码中的类型信息。 TypeNode 及其子类型 专门用于表示各种 Java 数据类型，并可能提供一些
与类型相关的操作或属性，例如：

获取类型的名称 (TypeName)。
判断类型是否是基本数据类型、引用类型、数组类型等。
获取类型的维度 (对于数组类型)。
获取泛型类型的类型参数信息。

3. 类比理解:

我们可以用 “类别” 和 “子类别” 的概念来类比 JavaNode 和 TypeNode：

JavaNode 就像一个大的 “类别”  (例如 “交通工具”):  它包含了所有可能的 Java 语法元素，就像 “交通工具”
类别包含了汽车、火车、飞机、轮船、自行车等各种类型的交通工具。

TypeNode 就像 “类别” 的一个 “子类别” (例如 “陆地交通工具”):  它只关注 Java 语法元素中表示 “类型” 的部分，就像 “陆地交通工具”
子类别只包含汽车、火车、自行车等在陆地上行驶的交通工具，而排除了飞机、轮船等。 TypeNode 是 JavaNode 的一个更具体的、更专业的子集。

4. AST 树中的位置关系:

在 PMD 的 AST 树中：

ASTCompilationUnit 是根节点 (一种 JavaNode).
ASTClassDeclaration, ASTMethodDeclaration, ASTFieldDeclaration, ASTStatement, ASTExpression 等都是 JavaNode 的子类型，用于表示不同的
Java 语法结构。
ASTPrimitiveType, ASTClassOrInterfaceType, ASTArrayType 等都是 TypeNode 的子类型， 同时也是 JavaNode 的子类型。 这意味着，所有
TypeNode 都是 JavaNode，但并非所有 JavaNode 都是 TypeNode。
总结:

JavaNode 是一个更广泛的概念，代表 PMD AST 中所有 Java 语法节点的通用类型。 它是 AST 树的基本构建块，提供了通用的节点操作和访问接口。
TypeNode 是 JavaNode 的一个更具体的子类型，专门用于表示 Java 代码中的数据类型。 TypeNode 及其子类型 (如 ASTPrimitiveType,
ASTClassOrInterfaceType, ASTArrayType) 用于在 AST 中精确地表示各种 Java 类型信息。
简单来说，如果你需要处理任何 Java 语法结构，你可能会用到 JavaNode。 如果你特别关注代码中的数据类型信息，你需要使用 TypeNode
及其子类型。 TypeNode 可以看作是 JavaNode 中专门用于处理类型信息的一个分支。

-----------------------------------------------------------------------------------------------------------------------------v
ASTMethodDeclaration 和 ASTMethodCall 在 PMD (以及 Java 语法) 中代表着 截然不同的概念，它们分别对应着 Java 代码中方法的
定义 (Declaration) 和 调用 (Call)。

我们可以用一个比喻来帮助理解：如果把方法比作一个 “食谱”，那么 ASTMethodDeclaration 就是 “食谱的描述”，而 ASTMethodCall 就是
“按照食谱做菜”。

下面我们从几个方面详细解释它们之间的区别：

1. 目的和含义 (Purpose and Meaning):

ASTMethodDeclaration (方法声明抽象语法树节点)：

目的： 代表 方法的定义。 它描述了 方法的结构和组成，包括方法名、参数、返回类型、方法体、修饰符等等。
含义： ASTMethodDeclaration 节点对应于 Java 源代码中 方法的声明部分，也就是 定义方法的那段代码。
类比： 就像 “食谱” 本身，它详细列出了做一道菜的原料、步骤、烹饪时间等等，但食谱本身并不会 “做菜”。
ASTMethodCall (方法调用抽象语法树节点)：

目的： 代表 方法的调用。 它表示在代码的某个地方 执行 (调用) 一个已经定义好的方法。
含义： ASTMethodCall 节点对应于 Java 源代码中 调用方法的那段代码，也就是 实际使用方法的地方。
类比： 就像 “按照食谱做菜” 的动作，你根据食谱的指示，准备食材，按照步骤操作，最终做出一道菜。 方法调用就是执行方法定义中描述的步骤。

2. 上下文 (Context):

ASTMethodDeclaration:

出现位置： 通常出现在 类声明 (ASTClassDeclaration) 或接口声明 (ASTInterfaceDeclaration) 的内部。 方法是类或接口的成员。

源代码位置： 对应于 Java 源代码中 方法定义的代码块，例如：

Java

public class MyClass {
// 方法声明 (ASTMethodDeclaration) 在这里
public void myMethod(int param1, String param2) {
// 方法体
System.out.println("Method called!");
}
}
ASTMethodCall:

出现位置： 可以出现在 任何语句 (Statement) 或表达式 (Expression)  允许出现的地方。 例如，方法调用可以作为表达式语句、赋值语句的一部分、条件语句的一部分等等。

源代码位置： 对应于 Java 源代码中 调用方法的那段代码，例如：

Java

public class MyClass {
public void myMethod(int param1, String param2) {
System.out.println("Method called!");
}

    public void anotherMethod() {
        // 方法调用 (ASTMethodCall) 在这里
        myMethod(10, "example");
        int result = calculateValue(5); // 方法调用 (ASTMethodCall) 在这里
    }

    public int calculateValue(int input) {
        return input * 2;
    }

}

3. 存储的信息 (Information Stored):

ASTMethodDeclaration:  主要存储 方法定义的信息，例如：

方法名 (Method Name)：例如 myMethod, calculateSum。
修饰符 (Modifiers)：例如 public, private, static, void 返回类型等。
形式参数 (Formal Parameters)：参数列表，包括参数类型和名称。
方法体 (Method Body)：方法 {} 内部的代码块，由一系列语句组成。
异常声明 (Throws Clause)：方法声明中 throws 关键字后面声明的异常类型列表。
ASTMethodCall:  主要存储 方法调用的信息，例如：

方法名 (Method Name)：被调用的方法名称，例如 println, getName, calculateSum。
参数列表 (Argument List)：方法调用时实际传递的参数值 (表达式列表)。
方法调用的对象或类 (可选)：对于成员方法调用 (例如 object.method()) 或静态方法调用 (例如 Class.staticMethod())
，会记录方法调用的对象或类 (表达式)。
类型实参列表 (可选，用于泛型方法调用)：如果调用的是泛型方法，可能会记录显式指定的类型参数。

4. 类比总结 (Analogy Summary):

特性 ASTMethodDeclaration ASTMethodCall
代表 方法的 定义 (Declaration)    方法的 调用 (Call)
目的 描述方法的结构和组成 表示执行 (使用) 已定义的方法
源代码位置 方法定义的代码块 调用方法的代码位置
存储信息 方法名、修饰符、参数、返回类型、方法体等 方法名、参数值、调用对象/类 (可选) 等
类比 食谱 按照食谱做菜的动作

Export to Sheets
简单记忆：

Declaration (声明) -> Definition (定义) -> ASTMethodDeclaration: 关注方法的 “是什么” (What)，描述方法的结构。
Call (调用) -> Invocation (执行) -> ASTMethodCall: 关注方法的 “怎么用” (How)，表示在代码中实际使用方法。
在 PMD 规则开发中，你可能会同时处理 ASTMethodDeclaration 和 ASTMethodCall 节点：

ASTMethodDeclaration: 当你需要分析 方法本身的特性 时，例如方法的复杂度、代码长度、参数个数、修饰符使用等，你会访问
ASTMethodDeclaration 节点。
ASTMethodCall: 当你需要分析 方法的使用情况 时，例如检查特定方法的调用是否符合规范、统计特定方法的调用次数、分析方法调用链等，你会访问
ASTMethodCall 节点。
理解 ASTMethodDeclaration 和 ASTMethodCall 的区别是进行 Java 代码静态分析的基础，尤其是在开发 PMD
规则时，能够帮助你更精确地定位和分析代码中的方法定义和方法调用。