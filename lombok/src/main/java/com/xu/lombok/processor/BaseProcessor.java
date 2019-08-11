package com.xu.lombok.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 所有需要处理注解的类都需要继承该类
 */
public class BaseProcessor extends AbstractProcessor {

    /**
     * 用于在编译器打印消息的组件，Messager主要是用来在编译期打log用的
     */
    Messager messager;

    /**
     * 语法树，JavacTrees提供了待处理的抽象语法树
     */
    JavacTrees trees;

    /**
     * 用来构造语法树节点，//TreeMaker封装了创建AST节点的一些方法
     */
    TreeMaker treeMaker;

    /**
     * 用于创建标识符的对象，Names提供了创建标识符的方法
     */
    Names names;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    /**
     * 获取一些注解处理器执行处理逻辑时需要用到的一些关键对象
     * @param processingEnv
     */
    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }
}
