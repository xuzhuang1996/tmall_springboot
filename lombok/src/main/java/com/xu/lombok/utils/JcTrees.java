package com.xu.lombok.utils;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Modifier;
import java.util.Set;

//工具类
public final class JcTrees {
    /**
     * 构造器名称
     */
    public static final String CONSTRUCTOR_NAME = "<init>";


    /**
     * 是否为构造器
     * @param jcMethodDecl
     * @return
     */
    public static boolean isConstructor(JCTree.JCMethodDecl jcMethodDecl) {
        String name = jcMethodDecl.name.toString();
        if(CONSTRUCTOR_NAME.equals(name)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为共有方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isPublicMethod(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers jcModifiers = jcMethodDecl.getModifiers();
        Set<Modifier> modifiers =  jcModifiers.getFlags();
        if(modifiers.contains(Modifier.PUBLIC)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为私有方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isPrivateMethod(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers jcModifiers = jcMethodDecl.getModifiers();
        Set<Modifier> modifiers =  jcModifiers.getFlags();
        if(modifiers.contains(Modifier.PRIVATE)) {
            return true;
        }
        return false;
    }

    /**
     * 是否为无参方法
     * @param jcMethodDecl
     * @return
     */
    public static boolean isNoArgsMethod(JCTree.JCMethodDecl jcMethodDecl) {
        List<JCTree.JCVariableDecl> jcVariableDeclList = jcMethodDecl.getParameters();
        return jcVariableDeclList == null || jcVariableDeclList.size() == 0;
    }
}
