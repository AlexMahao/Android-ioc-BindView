package com.example;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 *
 *  保存一个类的代理
 * Created by mahao on 17-3-9.
 */
public class ProxyInfo {

    private String packageName;

    private String proxyClassName;

    private TypeElement typeElement;

    // 注入的id和成员变量
    public Map<Integer, VariableElement> injectVariables = new HashMap<>();

    public static final String PROXY = "Finder";

    public ProxyInfo(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        packageName = packageElement.getQualifiedName().toString();
        proxyClassName = ClassValidator.getClassName(classElement, packageName) + "$$" + PROXY;
    }
    // 生成java 代码
    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("import com.mahao.ioc_api.*;\n");
        builder.append('\n');
        builder.append("public class ").append(proxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + typeElement.getQualifiedName() + ">");
        builder.append(" {\n");
        // 添加inject()方法
        generateMethods(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    // 生成注入的方法
    private void generateMethods(StringBuilder builder) {
        builder.append("@Override\n ");
        builder.append("public void inject(" + typeElement.getQualifiedName() + " host, Object source, Provider provider) {\n");
        for (int id : injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            builder.append("(" + type + ")(provider.findView( source," + id + "));\n");
        }
        builder.append("  }\n");
    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

}

