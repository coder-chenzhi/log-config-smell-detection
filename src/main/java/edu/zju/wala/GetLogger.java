package edu.zju.wala;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.util.config.AnalysisScopeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final List<String> filterClasses = new ArrayList<String>(Arrays.asList("Lsun/swing", "Ljava/swing", "Ljavafx", "Ljava/applet", "Ljava/awt"));
        for (String s : filterClasses) {
            if (clazz.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Boolean> getClasspath(String project, String root) throws IOException {
        String classpathFile = "wala/{project}_classpath.txt".replace("{project}", project.toLowerCase());
        File classPath = new File(GetLogger.class.getClassLoader().getResource(classpathFile).getFile());
        String fileContent =  Files.toString(classPath, Charsets.UTF_8).replace("{project_root}", root);
        Map<String, Boolean> classpathEntries = new HashMap<>();
        for (String line : fileContent.split(System.lineSeparator())) {
            if (line.split(" ").length == 2) {
                if (line.split(" ")[1].toLowerCase().equals("internal")) {
                    classpathEntries.put(line.split(" ")[0], true);
                } else {
                    classpathEntries.put(line.split(" ")[0], false);
                }
            }
        }
        return classpathEntries;
    }

    public static void retriveLogger(String projectName, String projectRoot) throws Exception {
        // 1.create an analysis scope representing the source file application
        File exFile = new File(GetLogger.class.getClassLoader().getResource("wala/no_exclusion.txt").getFile());
        Map<String, Boolean> classpathEntries = getClasspath(projectName, projectRoot);
        String classpath = classpathEntries.keySet().stream().collect(Collectors.joining(File.pathSeparator));
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, exFile);
        // 2. build a class hierarchy, call graph, and system dependence graph
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        LOG.info(cha.getNumberOfClasses() + " classes");

        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        IClass clazz;
        while (classes.hasNext()) {
            clazz = classes.next();
            if (filter(clazz.toString())) {
                continue;
            }
            String loggerScope = "Internal";
            // check internal or external
            try {
                JarFileEntry moduleEntry = (JarFileEntry) ((ShrikeClass) clazz).getModuleEntry();
                String jarFile = moduleEntry.getJarFile().getName().replace("\\", "/");
                LOG.debug("Found {} in {}", clazz.toString(), jarFile);
                if (classpathEntries.containsKey(jarFile)) {
                    if (classpathEntries.get(jarFile)) {
                        loggerScope = "Internal";
                    } else {
                        loggerScope = "External";
                    }
                } else {
                    // if there is no match, maybe jvm related jar
                    loggerScope = "External";
                }
            } catch (Exception e) {
                // if the clazz are loaded from .class file, then we will fail to get the jar file in this way
                // we assume the .class file are all internal file here
                LOG.info("Throw exception when extracting the jarfile of {}", clazz.toString());
            }
//            if (!clazz.getName().toString().toLowerCase().contains("cassandra")) {
//                continue;
//            }
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
                            String className = callee.getDeclaringClass().getName().toString();
                            String methodName = callee.getName().toString();

                            if (GetLogger.LoggerFunctions.containsKey(className + "." + methodName)) {
                                String libraryName = GetLogger.LoggerFunctions.get(className + "." + methodName);
                                if (instruction.getNumberOfUses() != 0) {
                                    int varIndex = instruction.getUse(0);
                                    String loggerName = "Unknown";
                                    if (ir.getSymbolTable().isStringConstant(varIndex)) {
                                        loggerName = ir.getSymbolTable().getValueString(varIndex);
                                        LOG.info("\t\tDetected {} {} logger naming by string:\t{}", loggerScope, libraryName, loggerName);
                                    } else {
                                        // Every class literal will generate a SSALoadMetadataInstruction,
                                        // which will include the name of class literal.
                                        // SSALoadMetadataInstruction will return a variable,
                                        // which will be used by  getLogger() method.
                                        SSAInstruction defineInst = cache.getDefUse(ir).getDef(varIndex);
                                        if (defineInst instanceof SSALoadMetadataInstruction) {
                                            loggerName = ((SSALoadMetadataInstruction) defineInst).getToken().toString();
                                            LOG.info("\t\tDetected {} {} logger naming by class literal:\t {}", loggerScope, libraryName, loggerName);
                                        } else {
                                            LOG.info("\t\tDetected {} {} logger but unknown name", loggerScope, libraryName);
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

    }

    public static void main(String[] args) throws Exception {
        retriveLogger("cassandra", "E:\\temp\\apache-cassandra-3.11.3");
    }
}
