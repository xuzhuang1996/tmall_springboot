package com.xu.lombok.utils;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Modifier;
import java.util.Set;

//工具类
public final class ProcessUtil {
    /**
     * 构造器名称
     */
    public static final String CONSTRUCTOR_NAME = "<init>";
    /**
     * 创建建造者的静态方法名
     */
    public static final String BUILDER_STATIC_METHOD_NAME = "builder";

    /**
     * 建造方法名
     */
    public static final String BUILD_METHOD_NAME = "build";

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

    /**
     * 判断是否存在无参构造方法
     *
     * @param jcClass 类的语法树节点
     * @return 是否存在
     */
    public static boolean hasNoArgsConstructor(JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethod.name.toString())) {
                    if (jcMethod.params.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取字段的语法树节点的集合
     *
     * @param jcClass 类的语法树节点
     * @return 字段的语法树节点的集合
     */
    public static List<JCTree.JCVariableDecl> getJCVariables(JCTree.JCClassDecl jcClass) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();

        //遍历jcClass的所有内部节点，可能是字段，方法等等
        for (JCTree jcTree : jcClass.defs) {
            //找出所有set方法节点，并添加
            if (isValidField(jcTree)) {
                //注意这个com.sun.tools.javac.util.List的用法，不支持链式操作，更改后必须赋值
                jcVariables.append((JCTree.JCVariableDecl) jcTree);
            }
        }
        return jcVariables.toList();
    }

    /**
     * 判断是否是合法的字段。因为有些字段是静态的，或者final，。这些字段就不应该进行set等。。。
     *
     * @param jcTree 语法树节点
     * @return 是否是合法字段
     */
    private static boolean isValidField(JCTree jcTree) {
        if (jcTree.getKind().equals(JCTree.Kind.VARIABLE)) {
            JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) jcTree;

            Set<Modifier> flagSets = jcVariable.mods.getFlags();
            return (!flagSets.contains(Modifier.STATIC)
                    && !flagSets.contains(Modifier.FINAL));
        }
        return false;
    }

    /**
     * 是否存在全参的构造方法
     *
     * @param jcVariables 字段的语法树节点集合
     * @param jcClass     类的语法树节点
     * @return 是否存在
     */
    public static boolean hasAllArgsConstructor(List<JCTree.JCVariableDecl> jcVariables, JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(JCTree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethod = (JCTree.JCMethodDecl) jcTree;
                if (CONSTRUCTOR_NAME.equals(jcMethod.name.toString())) {
                    if (jcVariables.size() == jcMethod.params.size()) {
                        boolean isEqual = true;
                        for (int i = 0; i < jcVariables.size(); i++) {
                            if (!jcVariables.get(i).vartype.type.equals(jcMethod.params.get(i).vartype.type)) {
                                isEqual = false;
                                break;
                            }
                        }
                        if (isEqual) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 克隆一个字段的语法树节点集合，作为方法的参数列表
     *
     * @param treeMaker            语法树节点构造器
     * @param prototypeJCVariables 字段的语法树节点集合
     * @return 方法参数的语法树节点集合
     */
    public static List<JCTree.JCVariableDecl> cloneJCVariablesAsParams(TreeMaker treeMaker, List<JCTree.JCVariableDecl> prototypeJCVariables) {
        ListBuffer<JCTree.JCVariableDecl> jcVariables = new ListBuffer<>();
        for (JCTree.JCVariableDecl jcVariable : prototypeJCVariables) {
            jcVariables.append(
                    treeMaker.VarDef(
                            treeMaker.Modifiers(Flags.PARAMETER),
                            jcVariable.name, //名字
                            jcVariable.vartype, //类型
                            null //初始化语句
            ));
        }
        return jcVariables.toList();
    }


}
