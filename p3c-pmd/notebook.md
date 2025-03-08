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