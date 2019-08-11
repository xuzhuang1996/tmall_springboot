package com.xu.lombok.processor;


import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.xu.lombok.anno.NoArgsConstructor;
import com.xu.lombok.utils.ProcessUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"com.xu.lombok.anno.NoArgsConstructor"})//指定需要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//指定jdk版本
public class BeanNoArgsConstructorProcessor extends  BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //首先获取被NoArgsConstructor注解标记的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(NoArgsConstructor.class);
        set.forEach(element -> {
            //获取当前元素的JCTree对象
            JCTree jcTree = trees.getTree(element);
            //JCTree利用的是访问者模式，将数据与数据的处理进行解耦，TreeTranslator就是访问者，这里我们重写访问类时的逻辑
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClass) {
                    //添加无参构造方法
                    if (!ProcessUtil.hasNoArgsConstructor(jcClass)) {
                        jcClass.defs = jcClass.defs.append(
                                createNoArgsConstructor()
                        );
                    }
                }
            });
        });
        return true;
    }

    /**
     * 创建无参数构造方法
     *
     * @return 无参构造方法语法树节点
     */
    private JCTree.JCMethodDecl createNoArgsConstructor() {
        //方法体
        JCTree.JCBlock jcBlock = treeMaker.Block(
                0 //访问标志
                , List.nil() //所有的语句
        );
        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC), //访问标志
                names.fromString(ProcessUtil.CONSTRUCTOR_NAME), //名字
                treeMaker.TypeIdent(TypeTag.VOID), //返回类型.这里我试过，一般为null也行
                List.nil(), //泛型形参列表
                List.nil(), //参数列表
                List.nil(), //异常列表
                jcBlock, //方法体
                null //默认方法（可能是interface中的那个default）
        );
    }
}
