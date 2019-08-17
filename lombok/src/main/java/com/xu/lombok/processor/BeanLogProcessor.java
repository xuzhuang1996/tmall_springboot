package com.xu.lombok.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.xu.lombok.anno.Log;
import com.xu.lombok.utils.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;


//import:https://blog.csdn.net/nideshouhu/article/details/90757952
@SupportedAnnotationTypes({"com.xu.lombok.anno.Log"})//指定需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//指定jdk版本
public class BeanLogProcessor  extends BaseProcessor{

    /**
     * 字段的语法树节点的集合
     */
    private List<JCTree.JCVariableDecl> fieldJCVariables;

    //执行完该函数后，
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //利用roundEnv的getElementsAnnotatedWith方法过滤出被Log这个注解标记的类，并存入set
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Log.class);
        String name = Log.class.getName();//将要新增的变量的名称。这里对Log注解增强，因此名字就是Log
        //遍历这个set里的每一个元素
        set.forEach(element -> {
            //获取当前元素的JCTree对象
            JCTree jcTree = trees.getTree(element);
            //JCTree利用的是访问者模式，将数据与数据的处理进行解耦，TreeTranslator就是访问者，这里我们重写访问类时的逻辑
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    //更新变量集合
                    before(jcClass);
                    //对成员变量进行遍历，如果有名为Log的对象了，则抛出异常
                    for (JCTree.JCVariableDecl jcVariable : fieldJCVariables)  {
                        if (jcVariable.getKind().equals(Tree.Kind.VARIABLE)) {
                            if(jcVariable.name.toString().equals(name)){
                                try {
                                    throw new Exception("已存在Log变量,@Log注解无法提供Log变量");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    importPackage(element, Logger.class);
                    importPackage(element, LoggerFactory.class);
                    //添加Log
                    jcClass.defs = jcClass.defs.append(addLogObject(element.getSimpleName().toString()));
                    //然后再清除fieldJCVariables
                    after();
                    System.out.println("最终生成：===========/n"+jcClass);
                }
            });
        });
        return true;
    }
    /**
     * 导入一个包
     *
     * @param element     所在的类
     * @param importClass 要导入的包
     */
    private void importPackage(Element element, Class<?> importClass) {
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) trees.getPath(element).getCompilationUnit();
        JCTree.JCFieldAccess fieldAccess = treeMaker.Select(treeMaker.Ident(names.fromString(importClass.getPackage().getName())), names.fromString(importClass.getSimpleName()));
        JCTree.JCImport jcImport = treeMaker.Import(fieldAccess, false);
        ListBuffer<JCTree> imports = new ListBuffer<>();
        imports.append(jcImport);
        for (int i = 0; i < compilationUnit.defs.size(); i++  ) {
            imports.append(compilationUnit.defs.get(i));
        }
        compilationUnit.defs = imports.toList();
    }



    /**
     * 定义方法（啥函数都行）：处理变量的方法,
     * 准备写变量吧，private Logger Log = LoggerFactory.getLogger(GloabalExceptionHandler.class);
     */
    private JCTree.JCVariableDecl addLogObject(String targetClassName){
        //标识符，即Logger类型的变量。getCanonicalName：org.slf4j.Logger
        JCTree.JCIdent varType = treeMaker.Ident(names.fromString(Logger.class.getSimpleName()));
        //变量名
        Name name = names.fromString(Log.class.getSimpleName());
        //初始化
        JCTree.JCExpression initValue = treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString(LoggerFactory.class.getSimpleName())),
                                names.fromString("getLogger")
                        ),
                        List.of(treeMaker.Ident(names.fromString(targetClassName+".class"))) //传入的参数集合
        );
        return treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC + Flags.FINAL),
                        name,
                        varType,
                        initValue
        );
    }

    /**
     * 进行一些初始化工作
     *
     * @param jcClass 类的语法树节点.对于一个加了注解的类Element，获取其字段的语法树节点的集合
     */
    private void before(JCTree.JCClassDecl jcClass) {
        this.fieldJCVariables = ProcessUtil.getJCVariables(jcClass);
    }

    /**
     * 进行一些清理工作
     */
    private void after() {
        this.fieldJCVariables = null;
    }

}

