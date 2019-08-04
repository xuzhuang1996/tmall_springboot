package com.xu.lombok.processor;


import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import com.xu.lombok.anno.myBuilder;
import com.xu.lombok.utils.JcTrees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;


//有空看看https://blog.csdn.net/dap769815768/article/details/90448451
//该类为了实现Lombok的builder注解,
@SupportedAnnotationTypes({"com.xu.lombok.anno.myBuilder"})//指定需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//指定jdk版本
public class BeanBuilderProcessor extends AbstractProcessor {

    private Messager messager;//Messager主要是用来在编译期打log用的
    private JavacTrees trees;//JavacTrees提供了待处理的抽象语法树
    private TreeMaker treeMaker;//TreeMaker封装了创建AST节点的一些方法
    private Names names;//Names提供了创建标识符的方法

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //引入环境变量
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    //执行完该函数后，
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //利用roundEnv的getElementsAnnotatedWith方法过滤出被myBuilder这个注解标记的类，并存入set
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(myBuilder.class);
        //遍历这个set里的每一个元素
        set.forEach(element -> {
            // 只处理作用在类上的注解
            if (element.getKind() == ElementKind.CLASS) {
                addPrivateConstructor(element);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, element.getSimpleName()+"建筑者模式已建立好");
            }
        });
        return true;
    }

    //定义方法（啥函数都行）：处理变量的方法,建筑者模式需要增强一个方法:私有构造函数
    private void addPrivateConstructor(Element element){
        JCTree jcTree = trees.getTree(element);
        element.getSimpleName();//调试看一下是否为类名，待会构造函数需要类名
        //创建一个TreeTranslator，并重写其中的visitClassDef方法，这个方法处理遍历语法树得到的类定义部分jcClassDecl
        jcTree.accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                //修饰语，目前不知干啥用的
                jcClassDecl.mods = (JCTree.JCModifiers) this.translate((JCTree) jcClassDecl.mods);
                //正式的类参数
                jcClassDecl.typarams = this.translateTypeParams(jcClassDecl.typarams);
                //这个类扩展的类
                jcClassDecl.extending = (JCTree.JCExpression) this.translate((JCTree) jcClassDecl.extending);
                //这个类实现的接口
                jcClassDecl.implementing = this.translate(jcClassDecl.implementing);

                boolean hasPrivateConstructor = false;  //是否拥有私有构造器
                ListBuffer<JCTree> statements = new ListBuffer<>();
                List<JCTree> oldList = this.translate(jcClassDecl.defs);//jcClassDecl.defs存着所有成员变量与构造函数
                List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();//存放所有成员变量
                List<JCTree.JCTypeParameter> jcTypeParameterList = List.nil();//存参数类型

                //设置方法体
                ListBuffer<JCTree.JCStatement> jcStatements = new ListBuffer<>();
                //对成员变量与构造函数进行遍历
                for (JCTree jcTree : oldList) {
                    if (isPublicDefaultConstructor(jcTree)) {
                        continue;   //不添加共有默认构造器
                    }
                    if (isPrivateDefaultConstructor(jcTree)) {
                        hasPrivateConstructor = true;//判断是否已经有默认私有构造器
                    }
                    if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
                        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
                        //然后获取参数具体数据
                        JCTree.JCVariableDecl variableDecl = treeMaker
                                .VarDef(treeMaker.Modifiers(Flags.PARAMETER, List.nil()), jcVariableDecl.name, jcVariableDecl.vartype, null);
                        jcVariableDeclList = jcVariableDeclList.append(variableDecl);//必须经过这一步，直接添加jcVariableDecl，报错：参数类型没有
                        //设置方法体
                        //Assign是赋值操作，Select选中this节点，下的
                        jcStatements.append(treeMaker.Exec(
                                (treeMaker.Assign(treeMaker.Select(treeMaker.Ident(names.fromString("this")), variableDecl.name),
                                        treeMaker.Ident(variableDecl.name)))
                        ));
                    }
                    statements.append(jcTree);//将原来的东西添加进来
                }
                if (!hasPrivateConstructor) {

                    //定义方法结构。包括参数，返回类型、异常等
                    JCTree.JCMethodDecl constructor = treeMaker.MethodDef(
                            treeMaker.Modifiers(Flags.PUBLIC, List.<JCTree.JCAnnotation>nil()),//JCTree.JCModifiers mods：方法级别
                            names.fromString(JcTrees.CONSTRUCTOR_NAME),//name，函数名
                            null,//     JCTree.JCExpression  restype，返回类型
                            jcTypeParameterList,//   List < JCTree.JCTypeParameter > typarams//参数类型,不过目前没有用，全存在jcVariableDeclList里面了
                            jcVariableDeclList,// JCTree.JCVariableDecl params，构造函数的参数
                            List.<JCTree.JCExpression>nil(),//     List < JCTree.JCExpression > thrown
                            treeMaker.Block(0L, jcStatements.toList()),  //body，方法体
                            null    //defaultValue
                    );
                    //定义好函数后。。。
                    statements.append(constructor);
                    jcClassDecl.defs = statements.toList(); //更新
                }
                this.result = jcClassDecl;
            }
        });
    }

    private JCTree.JCClassDecl makeBuilderClassDecl(List<JCTree.JCVariableDecl> jcVariableDeclList){
        return null;
    }


    //===================================工具方法==============================================
    //是否为共有默认构造器
    private boolean isPublicDefaultConstructor(JCTree jcTree) {

        if (jcTree.getKind() == Tree.Kind.METHOD) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
            return JcTrees.isConstructor(jcMethodDecl)
                    && JcTrees.isNoArgsMethod(jcMethodDecl)
                    && JcTrees.isPublicMethod(jcMethodDecl);
        }
        return false;
    }

    //是否为私有默认构造器
    private boolean isPrivateDefaultConstructor(JCTree jcTree) {

        if (jcTree.getKind() == Tree.Kind.METHOD) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
            return JcTrees.isConstructor(jcMethodDecl)
                    && JcTrees.isNoArgsMethod(jcMethodDecl)
                    && JcTrees.isPrivateMethod(jcMethodDecl);
        }
        return false;
    }

}
