package com.xu.lombok.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.xu.lombok.anno.Log;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;


//https://blog.csdn.net/enweitech/article/details/51915532
@SupportedAnnotationTypes({"com.xu.lombok.anno.Log"})//指定需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//指定jdk版本
public class BeanLogProcessor  extends BaseProcessor{

    //执行完该函数后，
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //利用roundEnv的getElementsAnnotatedWith方法过滤出被Log这个注解标记的类，并存入set
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Log.class);
        String name = Log.class.getName();//将要新增的变量的名称。这里对Log注解增强，因此名字就是Log
        //遍历这个set里的每一个元素
        set.forEach(element -> {
            // 只处理作用在类上的注解.对每一个类，都添加对象
            if (element.getKind() == ElementKind.CLASS) {
                addLogObject(element, name);
            }
        });
        return true;
    }

    /**
     * 定义方法（啥函数都行）：处理变量的方法,
     */

    private void addLogObject(Element element, String name){
        JCTree jcTree = trees.getTree(element);
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


                ListBuffer<JCTree> statements = new ListBuffer<>();//存放类的所有东西
                List<JCTree> oldList = this.translate(jcClassDecl.defs);//jcClassDecl.defs存着所有成员变量与构造函数
                List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();//存放所有成员变量
                List<JCTree.JCTypeParameter> jcTypeParameterList = List.nil();//存参数类型

                //对成员变量进行遍历，如果有名为Log的对象了，则抛出异常
                for (JCTree jcTree : oldList) {
                    if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
                        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
                        if(jcVariableDecl.name.toString().equals(name)){
                            try {
                                throw new Exception();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    statements.append(jcTree);//将原来的东西添加进来
                }
                //准备写变量吧，private Logger logger = LoggerFactory.getLogger(GloabalExceptionHandler.class);
                //Modifiers类、变量、方法等的(修饰符,注解),即第一个参数是修饰符，第二个是该变量上的注解
                //(JCTree.JCIdent)Logger

                JCTree.JCExpression varType = treeMaker.Ident(names.fromString(name));
                JCTree.JCVariableDecl variableDecl = treeMaker
                        .VarDef(treeMaker.Modifiers(Flags.PRIVATE | Flags.STATIC, List.nil()), names.fromString(name), varType, null);
                statements.append(variableDecl);
                jcClassDecl.defs = statements.toList(); //更新
                this.result = jcClassDecl;
            }
        });
    }

}
