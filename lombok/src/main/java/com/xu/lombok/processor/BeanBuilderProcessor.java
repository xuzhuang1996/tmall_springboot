package com.xu.lombok.processor;


import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.xu.lombok.anno.myBuilder;
import com.xu.lombok.utils.ProcessUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;


//有空看看https://blog.csdn.net/dap769815768/article/details/90448451
//该类为了实现Lombok的builder注解,
@SupportedAnnotationTypes({"com.xu.lombok.anno.myBuilder"})//指定需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//指定jdk版本
public class BeanBuilderProcessor extends  BaseProcessor {

    /**
     * 类名
     */
    private Name className;

    /**
     * Builder模式中的类名，例如原始类是User，那么建造者类名就是UserBuilder
     */
    private Name builderClassName;

    /**
     * 字段的语法树节点的集合
     */
    private List<JCTree.JCVariableDecl> fieldJCVariables;

    /**
     * 插入式注解处理器的处理逻辑
     *
     * @param annotations 注解
     * @param roundEnv    环境
     * @return 处理结果
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //首先获取被Builder注解标记的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(myBuilder.class);
        set.forEach(element -> {

            //获取当前元素的JCTree对象
            JCTree jcTree = trees.getTree(element);

            //JCTree利用的是访问者模式，将数据与数据的处理进行解耦，TreeTranslator就是访问者，这里我们重写访问类时的逻辑
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    before(jcClass);
                    //添加builder方法
                    jcClass.defs = jcClass.defs.append(
                            createStaticBuilderMethod()
                    );
                    //添加静态内部类
                    jcClass.defs = jcClass.defs.append(
                            createJCClass()
                    );
                    after();
                }
            });
        });
        return true;
    }

    /**
     * 创建静态方法，即builder方法，返回静态内部类的实例
     *
     * @return builder方法的语法树节点
     */
    private JCTree.JCMethodDecl createStaticBuilderMethod() {
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        //添加Builder模式中的返回语句 " return new XXXBuilder(); "
        jcStatements.append(
                treeMaker.Return(
                        treeMaker.NewClass(
                                null, //尚不清楚含义
                                List.nil(), //泛型参数列表
                                treeMaker.Ident(builderClassName), //创建的类名
                                List.nil(), //参数列表
                                null //类定义，估计是用于创建匿名内部类
                        )
                )
        );
        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , jcStatements.toList() //所有的语句
        );
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC + Flags.STATIC), //访问标志
                names.fromString(ProcessUtil.BUILDER_STATIC_METHOD_NAME), //名字
                treeMaker.Ident(builderClassName), //返回类型
                List.nil(), //泛型形参列表
                List.nil(), //参数列表
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }

    /**
     * 创建一个类的语法树节点。作为Builder模式中的Builder类。
     *
     * @return 创建出来的类的语法树节点
     */
    private JCTree.JCClassDecl createJCClass() {
        ListBuffer<JCTree> jcTrees = new ListBuffer<>();

        //创建静态Builder类的时候，还有添加其字段与方法。用于Builder返回生成
        jcTrees.appendList(createVariables());
        jcTrees.appendList(createSetJCMethods());
        jcTrees.append(createBuildJCMethod());

        return treeMaker.ClassDef(
                treeMaker.Modifiers(Flags.PUBLIC + Flags.STATIC + Flags.FINAL), //访问标志.
                builderClassName, //名字
                List.nil(), //泛型形参列表
                null, //继承
                List.nil(), //接口列表
                jcTrees.toList()); //定义
    }

    /**
     * 根据方法集合创建对应的字段的语法树节点集合
     *
     * @return 静态内部类的字段的语法树节点集合
     */
    private List<JCTree> createVariables() {
        ListBuffer<JCTree> jcVariables = new ListBuffer<>();

        for (JCTree.JCVariableDecl fieldJCVariable : fieldJCVariables) {
            jcVariables.append(
                    treeMaker.VarDef(
                            treeMaker.Modifiers(Flags.PRIVATE), //访问标志
                            names.fromString((fieldJCVariable.name.toString())), //名字
                            fieldJCVariable.vartype //类型
                            , null //初始化语句
                    )
            );
        }
        return jcVariables.toList();
    }
    /**
     * 创建方法的语法树节点的集合。作为Builder模式中的setXXX方法
     *
     * @return 方法节点集合
     */
    private List<JCTree> createSetJCMethods() {
        ListBuffer<JCTree> setJCMethods = new ListBuffer<>();

        for (JCTree.JCVariableDecl fieldJCVariable : fieldJCVariables) {
            setJCMethods.append(createSetJCMethod(fieldJCVariable));
        }

        return setJCMethods.toList();
    }

    /**
     * 创建一个方法的语法树节点。作为Builder模式中的setXXX方法
     *
     * @return 方法节点
     */
    private JCTree.JCMethodDecl createSetJCMethod(JCTree.JCVariableDecl jcVariable) {
        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        //添加语句 " this.xxx = xxx; "
        jcStatements.append(
                treeMaker.Exec(
                        treeMaker.Assign(
                                treeMaker.Select(
                                        treeMaker.Ident(names.fromString("this")),
                                        names.fromString(jcVariable.name.toString())
                                ),
                                treeMaker.Ident(names.fromString(jcVariable.name.toString()))
                        )
                )
        );
        //添加Builder模式中的返回语句 " return this; "
        jcStatements.append(
                treeMaker.Return(
                        treeMaker.Ident(names.fromString("this")
                        )
                )
        );
        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , jcStatements.toList() //所有的语句
        );
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC), //访问标志
                names.fromString(jcVariable.name.toString()), //名字
                treeMaker.Ident(builderClassName), //返回类型
                List.nil(), //泛型形参列表
                List.of(
                        treeMaker.VarDef(
                                treeMaker.Modifiers(Flags.PARAMETER),
                                jcVariable.name, //名字
                                jcVariable.vartype, //类型
                                null )
                ),
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }


    /**
     * 创建build方法的语法树节点；静态类的builder方法
     *
     * @return build方法的语法树节点
     */
    private JCTree.JCMethodDecl createBuildJCMethod() {
        ListBuffer<JCTree.JCExpression> jcVariableExpressions = new ListBuffer<>();

        for (JCTree.JCVariableDecl jcVariable : fieldJCVariables) {
            jcVariableExpressions.append(
                    treeMaker.Select(
                            treeMaker.Ident(names.fromString("this")),
                            names.fromString(jcVariable.name.toString())
                    )
            );
        }

        ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();

        //添加返回语句 " return new XXX(arg1, arg2, ...); ".返回主体类
        jcStatements.append(
                treeMaker.Return(
                        treeMaker.NewClass(
                                null, //尚不清楚含义
                                List.nil(), //泛型参数列表
                                treeMaker.Ident(className), //创建的类名
                                jcVariableExpressions.toList(), //参数列表
                                null //类定义，估计是用于创建匿名内部类
                        )
                )
        );

        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , jcStatements.toList() //所有的语句
        );

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC), //访问标志
                names.fromString(ProcessUtil.BUILD_METHOD_NAME), //名字。静态类的builder方法名
                treeMaker.Ident(className), //返回类型
                List.nil(), //泛型形参列表
                List.nil(), //参数列表
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }


    /**
     * 进行一些初始化工作
     *
     * @param jcClass 类的语法树节点
     */
    private void before(JCTree.JCClassDecl jcClass) {
        this.className = names.fromString(jcClass.name.toString());
        this.builderClassName = names.fromString(this.className + "Builder");
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClass);
    }

    /**
     * 进行一些清理工作
     */
    private void after() {
        this.className = null;
        this.builderClassName = null;
        this.fieldJCVariables = null;
    }



//    //执行完该函数后，
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        //利用roundEnv的getElementsAnnotatedWith方法过滤出被myBuilder这个注解标记的类，并存入set
//        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(myBuilder.class);
//        //遍历这个set里的每一个元素
//        set.forEach(element -> {
//            // 只处理作用在类上的注解
//            if (element.getKind() == ElementKind.CLASS) {
//                addPrivateConstructor(element);
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, element.getSimpleName()+"建筑者模式已建立好");
//            }
//        });
//        return true;
//    }
//
//    //定义方法（啥函数都行）：处理变量的方法,建筑者模式需要增强一个方法:私有构造函数
//    private void addPrivateConstructor(Element element){
//        JCTree jcTree = trees.getTree(element);
//        element.getSimpleName();//调试看一下是否为类名，待会构造函数需要类名
//        //创建一个TreeTranslator，并重写其中的visitClassDef方法，这个方法处理遍历语法树得到的类定义部分jcClassDecl
//        jcTree.accept(new TreeTranslator() {
//            @Override
//            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
//                //修饰语，目前不知干啥用的
//                jcClassDecl.mods = (JCTree.JCModifiers) this.translate((JCTree) jcClassDecl.mods);
//                //正式的类参数
//                jcClassDecl.typarams = this.translateTypeParams(jcClassDecl.typarams);
//                //这个类扩展的类
//                jcClassDecl.extending = (JCTree.JCExpression) this.translate((JCTree) jcClassDecl.extending);
//                //这个类实现的接口
//                jcClassDecl.implementing = this.translate(jcClassDecl.implementing);
//
//                boolean hasPrivateConstructor = false;  //是否拥有私有构造器
//                ListBuffer<JCTree> statements = new ListBuffer<>();
//                List<JCTree> oldList = this.translate(jcClassDecl.defs);//jcClassDecl.defs存着所有成员变量与构造函数
//                List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();//存放所有成员变量
//                List<JCTree.JCTypeParameter> jcTypeParameterList = List.nil();//存参数类型
//
//                //设置方法体
//                ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
//                //对成员变量与构造函数进行遍历
//                for (JCTree jcTree : oldList) {
//                    if (isPublicDefaultConstructor(jcTree)) {
//                        continue;   //不添加共有默认构造器
//                    }
//                    if (isPrivateDefaultConstructor(jcTree)) {
//                        hasPrivateConstructor = true;//判断是否已经有默认私有构造器
//                    }
//                    if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
//                        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
//                        //然后获取参数具体数据
//                        JCTree.JCVariableDecl variableDecl = treeMaker
//                                .VarDef(treeMaker.Modifiers(Flags.PARAMETER, List.nil()), jcVariableDecl.name, jcVariableDecl.vartype, null);
//                        jcVariableDeclList = jcVariableDeclList.append(variableDecl);//必须经过这一步，直接添加jcVariableDecl，报错：参数类型没有
//                        //设置方法体
//                        //Assign是赋值操作，Select选中this节点，下的
//                        jcStatements.append(treeMaker.Exec(
//                                (treeMaker.Assign(treeMaker.Select(treeMaker.Ident(names.fromString("this")), variableDecl.name),
//                                        treeMaker.Ident(variableDecl.name)))
//                        ));
//                    }
//                    statements.append(jcTree);//将原来的东西添加进来
//                }
//                if (!hasPrivateConstructor) {
//
//                    //定义方法结构。包括参数，返回类型、异常等
//                    JCTree.JCMethodDecl constructor = treeMaker.MethodDef(
//                            treeMaker.Modifiers(Flags.PUBLIC, List.<JCTree.JCAnnotation>nil()),//JCTree.JCModifiers mods：方法级别
//                            names.fromString(ProcessUtil.CONSTRUCTOR_NAME),//name，函数名
//                            null,//     JCTree.JCExpression  restype，返回类型
//                            jcTypeParameterList,//   List < JCTree.JCTypeParameter > typarams//参数类型,不过目前没有用，全存在jcVariableDeclList里面了
//                            jcVariableDeclList,// JCTree.JCVariableDecl params，构造函数的参数
//                            List.<JCTree.JCExpression>nil(),//     List < JCTree.JCExpression > thrown
//                            treeMaker.Block(0L, jcStatements.toList()),  //body，方法体
//                            null    //defaultValue
//                    );
//                    //定义好函数后。。。
//                    statements.append(constructor);
//                    jcClassDecl.defs = statements.toList(); //更新
//                }
//                this.result = jcClassDecl;
//            }
//        });
//    }
//
//    private JCTree.JCClassDecl makeBuilderClassDecl(List<JCTree.JCVariableDecl> jcVariableDeclList){
//        return null;
//    }
//
//
//    //===================================工具方法==============================================
//    //是否为共有默认构造器
//    private boolean isPublicDefaultConstructor(JCTree jcTree) {
//
//        if (jcTree.getKind() == Tree.Kind.METHOD) {
//            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
//            return ProcessUtil.isConstructor(jcMethodDecl)
//                    && ProcessUtil.isNoArgsMethod(jcMethodDecl)
//                    && ProcessUtil.isPublicMethod(jcMethodDecl);
//        }
//        return false;
//    }
//
//    //是否为私有默认构造器
//    private boolean isPrivateDefaultConstructor(JCTree jcTree) {
//
//        if (jcTree.getKind() == Tree.Kind.METHOD) {
//            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
//            return ProcessUtil.isConstructor(jcMethodDecl)
//                    && ProcessUtil.isNoArgsMethod(jcMethodDecl)
//                    && ProcessUtil.isPrivateMethod(jcMethodDecl);
//        }
//        return false;
//    }

}
