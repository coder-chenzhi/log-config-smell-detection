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
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class GetLogger {
    public static final Logger LOG = LoggerFactory.getLogger(GetLogger.class);

    private static final Map<String, String> LoggerFunctions = new HashMap<String, String>() {
        // @CommonsLog: org.apache.commons.logging.LogFactory.getLog(LogExample.class)
        // @JBossLog: org.jboss.logging.Logger.getLogger(LogExample.class);
        // @JUL: java.util.logging.Logger.getLogger(LogExample.class.getName());
        // @Log4j: org.apache.log4j.Logger.getLogger(LogExample.class);
        // @Log4j2: org.apache.logging.log4j.LogManager.getLogger(LogExample.class);
        // @Slf4j: org.slf4j.LoggerFactory.getLogger(LogExample.class);
        {
            put("Lorg/apache/commons/logging/LogFactory.getLog", "CommonsLog");
            put("Lorg/jboss/logging/Logger.getLogger", "JBossLog");
            put("Ljava/util/logging/Logger.getLogger", "JUL");
            put("Lorg/apache/log4j/Logger.getLogger", "Log4j");
            put("Lorg/apache/logging/log4j/LogManager.getLogger", "Log4j2");
            put("Lorg/slf4j/LoggerFactory.getLogger", "Slf4j");
        }
    };

    private static Boolean filter(String clazz) {
        final List<String> filterClasses = new ArrayList<String>(Arrays.asList("Lsun/swing", "Ljava", "Ljavafx"));
        for (String s : filterClasses) {
            if (clazz.startsWith(s)) {
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

    private static Set<IClass> retriveLoggerFromClass(IClass clazz, ClassHierarchy cha, IAnalysisCacheView cache) {
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

    public static void retriveLogger(String projectName, Map<String, String> projectsRoot) throws Exception {
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

            //cha.getImplementors(clazz.getReference());
            //cha.computeSubClasses(clazz.getReference());
            if (filter(clazz.toString())) {
                continue;
            }
            // check internal or external
            try {
                JarFileEntry moduleEntry = (JarFileEntry) ((ShrikeClass) clazz).getModuleEntry();
                String jarFile = moduleEntry.getJarFile().getName().replace("\\", "/");
                LOG.debug("Found {} in {}", clazz.toString(), jarFile);
                if (classpathEntries.containsKey(jarFile)) {
                    String loggerScope = classpathEntries.get(jarFile);
                    if ("Internal".equals(loggerScope)) {
                        internalClasses.add(clazz);
                    }
                    // if it belongs to mixed jar, which means this jar contains both internal classes and external classes
                    // simply check whether the class' name contains project's name
                    if ("Mixed".equals(loggerScope)) {
                        if (clazz.toString().toLowerCase().contains(projectName.toLowerCase())) {
                            internalClasses.add(clazz);
                        }
                    }
                }
            } catch (Exception e) {
                // if the clazz are loaded from .class file, then we will fail to get the jar file in this way
                // we assume the .class file are all internal file here
                LOG.info("Throw exception when extracting the jarfile of {}", clazz.toString());
            }
        }
        LOG.info("Number of internal class: {}", internalClasses.size());

        // handle internal classes
        for (IClass clazz : internalClasses) {
            LOG.info("class:\t" + clazz);
            for(IMethod m: clazz.getDeclaredMethods()) {
                // skip abstract method
                if (m.isAbstract()) {
                    continue;
                }
                // no need to handle (static) field initialization code,
                // because all static initialization code are in <clinit> named method
                LOG.info("\tmethod:\t" + m);
                try {
                    IR ir = cache.getIR(m); // can be null
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod callee = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget()); // can be null
                            if (callee == null) {
                                if (LOG.isDebugEnabled()) {
                                    IBytecodeMethod method = (IBytecodeMethod) ir.getMethod();
                                    int bytecodeIndex = method.getBytecodeIndex(i);
                                    int sourceLineNum = method.getLineNumber(bytecodeIndex);
                                    LOG.debug("Cannot find the declared target of method in source line {}", sourceLineNum);
                                }
                                continue;
                            }

                            IClass calleeClass = callee.getDeclaringClass();
                            String className = calleeClass.getName().toString();
                            String methodName = callee.getName().toString();
                            if (!internalClasses.contains(calleeClass)) {
                                Set<IClass> subtypes = new HashSet<>();
                                subtypes.add(calleeClass);
                                // find all subtype (subclass and implemented-class)
                                // skip Ljava/lang/Object, because all class are the subtype of Object
                                if (!className.contains("Ljava/lang/Object")) {
                                    subtypes.addAll(getSubtype(calleeClass, cha));
                                }
                                externalDirectInvokedClasses.addAll(subtypes.stream().
                                        filter(subtype -> !internalClasses.contains(subtype)).
                                        collect(Collectors.toList()));
                            }

                            if (GetLogger.LoggerFunctions.containsKey(className + "." + methodName)) {
                                String libraryName = GetLogger.LoggerFunctions.get(className + "." + methodName);
                                if (instruction.getNumberOfUses() != 0) {
                                    int varIndex = instruction.getUse(0);
                                    String loggerName = "Unknown";
                                    if (ir.getSymbolTable().isStringConstant(varIndex)) {
                                        loggerName = ir.getSymbolTable().getValueString(varIndex);
                                        LOG.info("\t\tDetected Internal {} logger naming by string:\t{}", libraryName, loggerName);
                                    } else {
                                        // Every class literal will generate a SSALoadMetadataInstruction,
                                        // which will include the name of class literal.
                                        // SSALoadMetadataInstruction will return a variable,
                                        // which will be used by  getLogger() method.
                                        SSAInstruction defineInst = cache.getDefUse(ir).getDef(varIndex);
                                        if (defineInst instanceof SSALoadMetadataInstruction) {
                                            loggerName = ((SSALoadMetadataInstruction) defineInst).getToken().toString();
                                            LOG.info("\t\tDetected Internal {} logger naming by class literal:\t {}", libraryName, loggerName);
                                        } else {
                                            LOG.info("\t\tDetected Internal {} logger but unknown name", libraryName);
                                        }
                                    }

                                } else {
                                    LOG.warn("\t\tthe number of parameters of getLogger is not 1");
                                }
                            }
                        }
                    }
                }catch (Throwable e){
                    LOG.error("\tError while creating IR for method: " + m.getReference(), e);
                }
            }

        }

        // TODO handle invoked external class
        LOG.info("ImmediateExternal classes:  {}", externalDirectInvokedClasses.size());
        Set<IClass> visitedClasses = new HashSet<>(internalClasses);
        while(!externalDirectInvokedClasses.isEmpty()) {
            IClass clazz = externalDirectInvokedClasses.iterator().next();
            externalDirectInvokedClasses.remove(clazz);
            visitedClasses.add(clazz);
            LOG.info("class:\t" + clazz);
            for(IMethod m: clazz.getDeclaredMethods()) {
                // skip abstract method
                if (m.isAbstract()) {
                    continue;
                }
                // no need to handle (static) field initialization code,
                // because all static initialization code are in <clinit> named method
                LOG.info("\tmethod:\t" + m);
                try {
                    IR ir = cache.getIR(m); // can be null
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod callee = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget()); // can be null
                            if (callee == null) {
                                if (LOG.isDebugEnabled()) {
                                    IBytecodeMethod method = (IBytecodeMethod) ir.getMethod();
                                    int bytecodeIndex = method.getBytecodeIndex(i);
                                    int sourceLineNum = method.getLineNumber(bytecodeIndex);
                                    LOG.debug("Cannot find the declared target of method in source line {}", sourceLineNum);
                                }
                                continue;
                            }

                            IClass calleeClass = callee.getDeclaringClass();
                            String className = calleeClass.getName().toString();
                            String methodName = callee.getName().toString();
                            // if not visited
                            if (!visitedClasses.contains(calleeClass)) {
                                Set<IClass> subtypes = new HashSet<>();
                                subtypes.add(calleeClass);
                                // find all subtype (subclass and implemented-class)
                                if (!className.contains("Ljava/lang/Object")) {
                                    subtypes.addAll(getSubtype(calleeClass, cha));
                                }
                                externalDirectInvokedClasses.addAll(subtypes.stream().
                                        filter(subtype -> !visitedClasses.contains(subtype)).
                                        collect(Collectors.toList()));


                                if (GetLogger.LoggerFunctions.containsKey(className + "." + methodName)) {
                                    String libraryName = GetLogger.LoggerFunctions.get(className + "." + methodName);
                                    if (instruction.getNumberOfUses() != 0) {
                                        int varIndex = instruction.getUse(0);
                                        String loggerName = "Unknown";
                                        if (ir.getSymbolTable().isStringConstant(varIndex)) {
                                            loggerName = ir.getSymbolTable().getValueString(varIndex);
                                            LOG.info("\t\tDetected External {} logger naming by string:\t{}", libraryName, loggerName);
                                        } else {
                                            // Every class literal will generate a SSALoadMetadataInstruction,
                                            // which will include the name of class literal.
                                            // SSALoadMetadataInstruction will return a variable,
                                            // which will be used by  getLogger() method.
                                            SSAInstruction defineInst = cache.getDefUse(ir).getDef(varIndex);
                                            if (defineInst instanceof SSALoadMetadataInstruction) {
                                                loggerName = ((SSALoadMetadataInstruction) defineInst).getToken().toString();
                                                LOG.info("\t\tDetected External {} logger naming by class literal:\t {}", libraryName, loggerName);
                                            } else {
                                                LOG.info("\t\tDetected External {} logger but unknown name", libraryName);
                                            }
                                        }

                                    } else {
                                        LOG.warn("\t\tthe number of parameters of getLogger is not 1");
                                    }
                                }
                            }
                        }
                    }
                }catch (Throwable e){
                    LOG.error("\tError while creating IR for method: " + m.getReference(), e);
                }
            }

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
            }
        };
        retriveLogger("zookeeper", projectsRoot);
    }
}
