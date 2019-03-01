package edu.zju.wala;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class GetLogger {
    private static final Logger LOG = LoggerFactory.getLogger(GetLogger.class);

    private static final Map<String, String> LoggerFunctions = new HashMap<String, String>() {
        // @CommonsLog: org.apache.commons.logging.LogFactory.getLog(LogExample.class)
        // @JBossLog: org.jboss.logging.Logger.getLogger(LogExample.class);
        // @JUL: java.util.logging.Logger.getLogger(LogExample.class.getName());
        // @Log4j: org.apache.log4j.Logger.getLogger(LogExample.class);
        // @Log4j2: org.apache.logging.log4j.LogManager.getLogger(LogExample.class);
        // @Slf4j: org.slf4j.LoggerFactory.getLogger(LogExample.class);
        // @TDDL: com.taobao.tddl.common.utils.logger.LoggerFactory.getLogger(LogExample.class);
        // @Jingwei: com.alibaba.middleware.jingwei.common.logger.LoggerFactory.getLogger(LogExample.class)
        // @Middleware: com.taobao.middleware.logger.LoggerFactory.getLogger(LogExample.class)
        // @InnerLog: com.alibaba.middleware.innerlog.LoggerFactory.getLogger(LogExample.class)
        {
            put("Lorg/apache/commons/logging/LogFactory.getLog", "CommonsLog");
            put("Lorg/jboss/logging/Logger.getLogger", "JBossLog");
            put("Ljava/util/logging/Logger.getLogger", "JUL");
            put("Lorg/apache/log4j/Logger.getLogger", "Log4j");
            put("Lorg/apache/logging/log4j/LogManager.getLogger", "Log4j2");
            put("Lorg/slf4j/LoggerFactory.getLogger", "Slf4j");
            put("Lcom/taobao/tddl/common/utils/logger/LoggerFactory.getLogger", "TDDL");
            put("Lcom/alibaba/middleware/jingwei/common/logger/LogFactory.getLogger", "Jingwei");
            put("Lcom/alibaba/middleware/jingwei/common/logger/JwLoggerFactoryV3.getLogger", "Jingwei");
            put("Lcom/alibaba/middleware/innerlog/LoggerFactory.getLogger", "InnerLog");
            put("Lcom/taobao/middleware/logger/LoggerFactory.getLogger", "Middleware");
        }
    };

    private static void getInitialValue(IAnalysisCacheView cache, IClass clazz) {
        for(IMethod method: clazz.getDeclaredMethods()) {
            if (method.getName().toString().contains("clinit")) {
                IR ir = cache.getIR(method);

            }
        }
    }

    private static Boolean filter(IClass clazz) {
        String classloader = clazz.getClassLoader().getName().toString();
        if ("Primordial".equals(classloader)) {
            return true;
        }
        String className = clazz.getName().toString();
        final List<String> filterClasses = new ArrayList<String>(
                Arrays.asList("Lsun/swing", "Ljava/swing", "Ljavax/swing", "Lcom/sun/swing",
                        "Lsun/awt", "Ljava/awt", "Lsun/applet", "Ljava/applet", "Lcom/sun/java/swing",
                        "Lorg/codehaus/groovy"));
        for (String s : filterClasses) {
            if (className.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> getClasspath(String project, Map<String, String> projectsRoot) throws IOException {
        String classpathFile = "wala/classpath/{project}_classpath.txt".replace("{project}", project.toLowerCase());
        File classPath = new File(GetLogger.class.getClassLoader().getResource(classpathFile).getFile());
        String fileContent =  Files.toString(classPath, Charsets.UTF_8);
        for (String key: projectsRoot.keySet()) {
            fileContent = fileContent.replace(key, projectsRoot.get(key));
        }
        Map<String, String> classpathEntries = new HashMap<>();
        for (String line : fileContent.split(System.lineSeparator())) {
            if (line.split(" ").length == 2) {
                classpathEntries.put(line.split(" ")[0], line.split(" ")[1]);
            }
        }
        return classpathEntries;
    }

    private static Set<String> buildInternalClassesFromSource(String srcRoot) {
        // TODO
        return null;
    }

    private static Set<IClass> getSubtype(IClass clazz, ClassHierarchy cha) {
        Set<IClass> classes = new HashSet<>();
//        if (clazz.getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
        classes.addAll(cha.getImplementors(clazz.getReference()));
        classes.addAll(cha.computeSubClasses(clazz.getReference()));
        return classes;
    }

    public static void retriveLogger(String projectName, Map<String, String> projectsRoot, String outputPath) throws Exception {
        // 0. prepare logger for output
        File output = new File(String.format("%s/%s.log", outputPath, projectName));
        if (output.exists()) {
            output.delete();
            Files.touch(output);
        }

        // 1.create an analysis scope representing the source file application
        File exFile = new File(GetLogger.class.getClassLoader().getResource("wala/no_exclusion.txt").getFile());
        Map<String, String> classpathEntries = getClasspath(projectName, projectsRoot);
        String classpath = String.join(File.pathSeparator, classpathEntries.keySet());
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, exFile);
        // 2. build a class hierarchy, call graph, and system dependence graph
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        LOG.info(cha.getNumberOfClasses() + " classes");

        Set<IClass> internalClasses = new HashSet<>();
        Set<IClass> externalDirectInvokedClasses = new HashSet<>();
        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        // 3. get all internal classes
        while (classes.hasNext()) {
            IClass clazz = classes.next();
            if (filter(clazz)) {
                LOG.debug("class {} has been filterd", clazz.toString());
                continue;
            }
            // check internal or external
            try {
                JarFileEntry moduleEntry = (JarFileEntry) ((ShrikeClass) clazz).getModuleEntry();
                String jarFile = moduleEntry.getJarFile().getName().replace("\\", "/");
                LOG.debug("Found {} in {}", clazz.toString(), jarFile);
                if (classpathEntries.containsKey(jarFile)) {
                    String loggerScope = classpathEntries.get(jarFile);
                    if ("internal".equals(loggerScope.toLowerCase())) {
                        internalClasses.add(clazz);
                    }
                    // if it belongs to mixed jar, which means this jar contains both internal classes and external classes
                    // simply check whether the class' name contains project's name
                    if ("mixed".equals(loggerScope.toLowerCase())) {
                        if (clazz.toString().toLowerCase().contains(projectName.toLowerCase())) {
                            internalClasses.add(clazz);
                        }
                    }
                }
            } catch (Exception e) {
                // if the clazz are loaded from .class file, then we will fail to get the jar file in this way
                // we assume the .class file are all internal file here
                internalClasses.add(clazz);
                LOG.info("Throw exception when extracting the jarfile of {}", clazz.toString());
            }
        }
        LOG.info("Number of internal class: {}", internalClasses.size());

        // handle internal classes
        for (IClass clazz : internalClasses) {
            LOG.info("class:\t" + clazz);
            for(IMethod method: clazz.getDeclaredMethods()) {
                // skip abstract method
                if (method.isAbstract()) {
                    continue;
                }
                // no need to handle (static) field initialization code,
                // because all static initialization code are in <clinit> named method
                LOG.debug("\tmethod:\t" + method);
                try {
                    IR ir = cache.getIR(method); // can be null
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod calleeMethod = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget()); // can be null
                            if (calleeMethod == null) {
                                if (LOG.isDebugEnabled()) {
                                    IBytecodeMethod m = (IBytecodeMethod) ir.getMethod();
                                    int bytecodeIndex = m.getBytecodeIndex(i);
                                    int sourceLineNum = m.getLineNumber(bytecodeIndex);
                                    LOG.debug("Cannot find the declared target of method in source line {}", sourceLineNum);
                                }
                                continue;
                            }

                            IClass calleeClass = calleeMethod.getDeclaringClass();
                            String calleeClassName = calleeClass.getName().toString();
                            String calleeMethodName = calleeMethod.getName().toString();
                            if (!internalClasses.contains(calleeClass)) {
                                Set<IClass> subtypes = new HashSet<>();
                                subtypes.add(calleeClass);
                                // find all subtype (subclass and implemented-class)
                                // skip Ljava/lang/Object, because all class are the subtype of Object
                                if (!calleeClassName.contains("Ljava/lang/Object")) {
                                    subtypes.addAll(getSubtype(calleeClass, cha));
                                }
                                externalDirectInvokedClasses.addAll(subtypes.stream().
                                        filter(subtype -> !internalClasses.contains(subtype)).
                                        collect(Collectors.toList()));
                            }

                            doExtract(true, clazz, method, calleeClassName, calleeMethodName, instruction, ir, cache, output);
                        }
                    }
                }catch (Throwable e){
                    LOG.error("\tError while creating IR for method: " + method.getReference(), e);
                }
            }

        }

        // handle invoked external class
        LOG.info("ImmediateExternal classes:  {}", externalDirectInvokedClasses.size());
        Set<IClass> visitedClasses = new HashSet<>(internalClasses);
        while(!externalDirectInvokedClasses.isEmpty()) {
            IClass clazz = externalDirectInvokedClasses.iterator().next();
            externalDirectInvokedClasses.remove(clazz);
            visitedClasses.add(clazz);
            if (filter(clazz)) {
                LOG.debug("class {} has been filterd", clazz.toString());
                continue;
            }
            LOG.info("class:\t" + clazz);
            for(IMethod method: clazz.getDeclaredMethods()) {
                // skip abstract method
                if (method.isAbstract()) {
                    continue;
                }
                // no need to handle (static) field initialization code,
                // because all static initialization code are in <clinit> named method
                LOG.debug("\tmethod:\t" + method);
                try {
                    IR ir = cache.getIR(method); // can be null
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod callee = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget()); // can be null
                            if (callee == null) {
                                if (LOG.isDebugEnabled()) {
                                    IBytecodeMethod m = (IBytecodeMethod) ir.getMethod();
                                    int bytecodeIndex = m.getBytecodeIndex(i);
                                    int sourceLineNum = m.getLineNumber(bytecodeIndex);
                                    LOG.debug("Cannot find the declared target of method in source line {}", sourceLineNum);
                                }
                                continue;
                            }

                            IClass calleeClass = callee.getDeclaringClass();
                            String calleeClassName = calleeClass.getName().toString();
                            String calleeMethodName = callee.getName().toString();
                            // if not visited
                            if (!visitedClasses.contains(calleeClass)) {
                                Set<IClass> subtypes = new HashSet<>();
                                subtypes.add(calleeClass);
                                // find all subtype (subclass and implemented-class)
                                if (!calleeClassName.contains("Ljava/lang/Object")) {
                                    subtypes.addAll(getSubtype(calleeClass, cha));
                                }
                                externalDirectInvokedClasses.addAll(subtypes.stream().
                                        filter(subtype -> !visitedClasses.contains(subtype)).
                                        collect(Collectors.toList()));

                                doExtract(false, clazz, method, calleeClassName, calleeMethodName, instruction, ir, cache, output);
                            }
                        }
                    }
                }catch (Throwable e){
                    LOG.error("\tError while creating IR for method: " + method.getReference(), e);
                }
            }

        }
    }

    private static void doExtract(Boolean internal, IClass callerClass, IMethod callerMethod, String calleeClassName,
                                  String calleeMethodName, SSAInstruction instruction, IR ir,
                                  IAnalysisCacheView cache, File output) {
        String scope = internal?"Internal":"External";
        String libraryName;
        // Check whether it is a method to get logger
        if (GetLogger.LoggerFunctions.containsKey(calleeClassName + "." + calleeMethodName)) {
            libraryName = GetLogger.LoggerFunctions.get(calleeClassName + "." + calleeMethodName);
        } else if ("getLog".equals(calleeMethodName) || "getLogger".equals(calleeMethodName)) {
            // the name of the method indicate that this method is to get logger
            libraryName = "Unknown";
        } else {
            return;
        }
//            if ("InnerLog".equals(libraryName)) {
//                System.out.println();
//            }
        String jarFile = "Unknown";
        try {
            JarFileEntry moduleEntry = (JarFileEntry) ((ShrikeClass) callerClass).getModuleEntry();
            jarFile = moduleEntry.getJarFile().getName().replace("\\", "/");
        } catch (Exception e) {

        }

        if (instruction.getNumberOfUses() != 0) {
            String naming = "Unknown";
            String loggerName = "Unknown";
            int varIndex = instruction.getUse(0);
            if (ir.getSymbolTable().isStringConstant(varIndex)) {
                loggerName = ir.getSymbolTable().getValueString(varIndex);
                naming = "naming by string";
            } else {
                // Every class literal will generate a SSALoadMetadataInstruction,
                // which will include the name of class literal.
                // SSALoadMetadataInstruction will return a variable,
                // which will be used by  getLogger() method.
                SSAInstruction defineInst = cache.getDefUse(ir).getDef(varIndex);
                if (defineInst instanceof SSALoadMetadataInstruction) {
                    loggerName = ((SSALoadMetadataInstruction) defineInst).getToken().toString();
                    naming = "naming by class literal";
                } else if (defineInst instanceof SSAInvokeInstruction) {
                    // handle some special case
                    // 1. private final Log log = LogFactory.getLog(getClass());
                    // 2. private final Log log = LogFactory.getLog(Main.class.getName());
                    // 3. private final Log log = LogFactory.getLog(this.getClass());
                    // all these three cases are taking the return value of some function
                    // as the logger, and the return value is the containing class of the function
                    MethodReference method = ((SSAInvokeInstruction) defineInst).getDeclaredTarget();
                    if ("getClass".equals(method.getName().toString())) {
                        loggerName = ((SSAInvokeInstruction) defineInst).getDeclaredTarget().getDeclaringClass().toString();
                        naming = "naming by class literal";
                    } if ("getName".equals(method.getName().toString())) {
                        if (defineInst.getNumberOfUses() != 0) {
                            SSAInstruction preDefineInst = cache.getDefUse(ir).getDef(defineInst.getUse(0));
                            if (preDefineInst instanceof SSALoadMetadataInstruction) {
                                loggerName = ((SSALoadMetadataInstruction) preDefineInst).getToken().toString();
                                naming = "naming by class literal";
                            }
                        }
                    }
                }
            }
            try {
                Files.append(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",
                        scope, libraryName, jarFile, callerMethod.toString(), naming, loggerName),
                        output, Charsets.UTF_8);
            } catch (IOException e) {
                LOG.warn("fail to write following record to file ");
                LOG.warn("{}\t{}\t{}\t{}\t{}\t{}", scope, libraryName, jarFile, callerMethod, naming, loggerName);
            }

        } else {
            LOG.warn("\t\tthe getLogger doesn't have parameters");
        }

    }

    public static void main(String[] args) throws Exception {
        Map<String, String> projectsRoot = new HashMap<String, String>() {
            {
                put("{activemq_root}", "/home/chenzhi/Data/projects/jar/apache-activemq-5.15.8");
                put("{ambari_root}", "/home/chenzhi/Data/projects/jar/ambari-server-2.7.3.0.0-dist");
                put("{cassandra_root}", "/home/chenzhi/Data/projects/jar/apache-cassandra-3.11.3-bin/apache-cassandra-3.11.3");
                put("{flume_root}", "/home/chenzhi/Data/projects/jar/apache-flume-1.8.0-bin");
                put("{hadoop_root}", "/home/chenzhi/Data/projects/jar/hadoop-2.9.2");
                put("{hbase_root}", "/home/chenzhi/Data/projects/jar/hbase-2.1.1");
                put("{hive_root}", "/home/chenzhi/Data/projects/jar/apache-hive-3.1.1-bin");
                put("{solr_root}", "/home/chenzhi/Data/projects/jar/solr-7.5.0");
                put("{storm_root}", "/home/chenzhi/Data/projects/jar/apache-storm-1.2.2");
                put("{zookeeper_root}", "/home/chenzhi/Data/projects/jar/zookeeper-3.4.13");
                put("{JAVA_HOME}", "/usr/lib/jvm/java-8-openjdk-amd64");
                put("{auctionplatform_root}", "/home/chenzhi/Data/projects/Prebuilt/auctionplatform");
                put("{buy2_root}", "/home/chenzhi/Data/projects/Prebuilt/buy2");
                put("{diamond_root}", "/home/chenzhi/Data/projects/Prebuilt/diamond");
                put("{fundplatform_root}", "/home/chenzhi/Data/projects/Prebuilt/fundplatform");
                put("{itemcenter_root}", "/home/chenzhi/Data/projects/Prebuilt/itemcenter");
                put("{jingwei3_root}", "/home/chenzhi/Data/projects/Prebuilt/jingwei-worker");
                put("{notify_root}", "/home/chenzhi/Data/projects/Prebuilt/notify");
                put("{tddl-server_root}", "/home/chenzhi/Data/projects/Prebuilt/tddl-server");
                put("{tlogserver_root}", "/home/chenzhi/Data/projects/Prebuilt/tlogserver");
                put("{tradeplatform_root}", "/home/chenzhi/Data/projects/Prebuilt/tradeplatform");
            }
        };
        String[] projects = new String[]{"activemq", "ambari", "cassandra", "flume", "hadoop", "hbase", "hive", "solr",
                "Storm", "zookeeper", "auctionplatform", "buy2", "diamond", "fundplatform", "itemcenter", "jingwei3",
                "notify", "tddl-server", "tlogserver", "tradeplatform"};

        projects = new String[]{"zookeeper"};
        String outputPath = "/home/chenzhi/IdeaProjects/logconfigsmelldetection/logs";

        for (String pro : projects) {
            retriveLogger(pro, projectsRoot, outputPath);
        }
    }
}
