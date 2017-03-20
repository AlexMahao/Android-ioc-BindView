package com.example;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *  - 注解类不能被继承
 *  - 被注解的类必须提供一个默认的构造方法（无参），不然无法实例化
 *  -
 *
 * Created by mahao on 17-3-9.
 */
@AutoService(Processor.class)
/**
 * @AutoService 是由Google开发的一个注解，
 *      用来生成META-INF/servcices/javax.annotation.processing.Processor文件
 *          在通过javac命令时，使用编译时注解，必须将注解打成相应的jar包，jar包中除了包含基本的类文件，还有如上的文件目录
 *
 *      - 文件目录
 *          IocProcessor.jar
            com
                example
                    MyProcessor.class
            META-INF
                services
                    javax.annotation.processing.Processor
 *
 *      - javax.annotation.processing.Processor 文件内容
 *              com.example.IocProcessor
 *
 *         全路径包名，标示需要处理的进程类
 *
 */
public class IocProcessor extends AbstractProcessor {

    // 文件相关的辅助类，生成JavaSouceCode
    private Filer mFileUtils;

    /**
     * 处理标签相关
     *   PakcageElement : 包
     *   TypeElement : 类
     *   VariableElement  : 变量
     *   ExecuteableElement : 方法（构造方法和普通方法）
     *
     *      getEnclosedELements() 获取他包裹的元素（子）
     *      getEnclosedELement() 获取包裹他的元素(父)
     */
    private Elements mElementUtils;
    // 日志相关
    private Messager mMessager;

    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFileUtils = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    /**
     * 标示该处理器捕获处理的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        // getCanonicalName 获取规范的名字
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }
    /**
     *  指定使用的java版本，一般默认支持返回最新
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();
    /**
     * 相当于main 函数，处理扫描，评估和处理注解的代码以及生成java文件
     * @param set
     * @param roundEnvironment 用于查询包含特定注解的注解元素
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mProxyMap.clear();
        // --------------------检索注解，保存包含注解的类以及需要注入的成员变量-------------------------
        // 该方法获取的element是被注解标注的所有元素，所以需要检查
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            // 检查element的类型
            if (!checkAnnotationValid(element, BindView.class)) {
                return false;
            }
            VariableElement variableElement = ((VariableElement) element);
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            // 全路径名
            String qualifiedName = typeElement.getQualifiedName().toString();
            // 类信息
            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }
            // 在类信息中保存需要注入的成员变量和id
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int id = annotation.value();
            proxyInfo.injectVariables.put(id, variableElement);
        }
        // ---------------- 生成对应的类----------------------------
        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                // processingEnv ： 注解处理环境（工具类），提供很多有用的功能工具类
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
                //----- 在生成类之后gradle会对代码进行优化
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }

        }
        return true;
    }

    /**
     * 检查查找的元素是否合法
     */
    private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.FIELD) {
            error(annotatedElement, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }
        return true;
    }

    // 展示错误信息
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
