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
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;
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

    private static Boolean filter(IClass clazz) {
        String classloader = clazz.getClassLoader().getName().toString();
        if ("Primordial".equals(classloader)) {
            return true;
        }
        String className = clazz.getName().toString();
        for (String s : Constants.FilteredPackage) {
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
        // 2. build a class hierarchy
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        LOG.info(cha.getNumberOfClasses() + " classes");

        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        while (classes.hasNext()) {
            String loggerScope = "External";
            IClass clazz = classes.next();
            if (filter(clazz)) {
                LOG.debug("class {} has been filtered", clazz.toString());
                continue;
            }
            LOG.info("class:\t" + clazz);
            // check internal or external
            try {
                JarFileEntry moduleEntry = (JarFileEntry) ((ShrikeClass) clazz).getModuleEntry();
                String jarFile = moduleEntry.getJarFile().getName().replace("\\", "/");
                LOG.debug("Found {} in {}", clazz.toString(), jarFile);
                if (classpathEntries.containsKey(jarFile)) {
                    loggerScope = classpathEntries.get(jarFile);
                    if ("internal".equals(loggerScope.toLowerCase())) {
                        loggerScope = "Internal";
                    }
                    // if it belongs to mixed jar, which means this jar contains both internal classes and external classes
                    // simply check whether the class' name contains project's name
                    if ("mixed".equals(loggerScope.toLowerCase())) {
                        if (clazz.toString().toLowerCase().contains(projectName.toLowerCase())) {
                            loggerScope = "Internal";
                        }
                    }
                }
            } catch (Exception e) {
                // if the clazz are loaded from .class file, then we will fail to get the jar file
                // we check the class name whether it contains "com/taobao" or "com/alibaba"
                if (clazz.toString().toLowerCase().contains("taobao") ||
                        clazz.toString().toLowerCase().contains("alibaba")) {
                    loggerScope = "Internal";
                }
                LOG.info("Throw exception when extracting the jarfile of {}", clazz.toString());
            }

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

                            doExtract(loggerScope, clazz, method, calleeClassName, calleeMethodName,
                                    instruction, ir, cha, cache, output);
                        }
                    }
                }catch (Throwable e){
                    LOG.error("\tError while creating IR for method: " + method.getReference(), e);
                }
            }
        }

    }

    private static void doExtract(String scope, IClass callerClass, IMethod callerMethod, String calleeClassName,
                                  String calleeMethodName, SSAInstruction instruction, IR ir, ClassHierarchy cha,
                                  IAnalysisCacheView cache, File output) {

        String libraryName;
        // Check whether it is a method to get logger
        if (Constants.LoggerFunctions.containsKey(calleeClassName + "." + calleeMethodName)) {
            libraryName = Constants.LoggerFunctions.get(calleeClassName + "." + calleeMethodName);
        } else if ("getLog".equals(calleeMethodName) || "getLogger".equals(calleeMethodName)) {
            //TODO Be careful, this operation is too aggressive
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
                    // TODO 4. private final Log log = LogFactory.getLog(Enum.type.getName());
                    // this case is naming logger by enum field access
                    MethodReference method = ((SSAInvokeInstruction) defineInst).getDeclaredTarget();
                    if ("getClass".equals(method.getName().toString())) {
                        // handle 1st and 3rd case
                        loggerName = ((SSAInvokeInstruction) defineInst).getDeclaredTarget().getDeclaringClass().toString();
                        if ("<Application,Ljava/lang/Object>".equals(loggerName)) {
                            // handle 3rd case, in this case, the logger class will be detected as java.lang.Object
                            loggerName = callerClass.toString();
                        }
                        naming = "naming by class literal";
                    } if ("getName".equals(method.getName().toString())) {
                        // handle 2nd case
                        if (defineInst.getNumberOfUses() != 0) {
                            SSAInstruction preDefineInst = cache.getDefUse(ir).getDef(defineInst.getUse(0));
                            if (preDefineInst instanceof SSALoadMetadataInstruction) {
                                loggerName = ((SSALoadMetadataInstruction) preDefineInst).getToken().toString();
                                naming = "naming by class literal";
                            }
                        }
                    }
                } else if (defineInst instanceof SSAGetInstruction) {
                    // handle static field access and enum field access
                    FieldReference field = ((SSAGetInstruction) defineInst).getDeclaredField();
                    if ("Ljava/lang/String".equals(field.getFieldType().getName().toString())) {
                        IClass clazz = cha.lookupClass(field.getDeclaringClass());
                        StaticStringFields.addClass(clazz, cache);
                        if (StaticStringFields.getStaticStringField(field) != null) {
                            naming = "naming by string";
                            loggerName = StaticStringFields.getStaticStringField(field);
                        }
                    }
                }
            }
            try {
                Files.append(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",
                        scope, libraryName, jarFile, callerMethod.toString(), naming, loggerName),
                        output, Charsets.UTF_8);
//                LOG.warn(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",
//                        scope, libraryName, jarFile, callerMethod.toString(), naming, loggerName));
            } catch (IOException e) {
                LOG.warn("fail to write following record to file ");
                LOG.warn("{}\t{}\t{}\t{}\t{}\t{}", scope, libraryName, jarFile, callerMethod, naming, loggerName);
            }

        } else {
            LOG.warn("{}\t{}\t{}\tthe getLogger doesn't have parameters", libraryName, jarFile, callerMethod);
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
                put("{tradeplatform3_root}", "/home/chenzhi/Data/projects/Prebuilt/tradeplatform3");
                put("test_root", "");
            }
        };


        String[] projects = new String[]{"activemq", "ambari", "cassandra", "flume", "hadoop", "hbase", "hive", "solr",
                "Storm", "zookeeper", "auctionplatform", "buy2", "diamond", "fundplatform", "itemcenter", "jingwei3",
                "notify", "tddl-server", "tlogserver", "tradeplatform"};

        projects = new String[]{"test"};
        String outputPath = "/home/chenzhi/IdeaProjects/logconfigsmelldetection/logs";

//        String prefix = "/media/chenzhi/7ae9463a-2a19-4d89-8179-d160bcb4ce1b";
//        outputPath = "" + outputPath;
//        for (String pro : projectsRoot.keySet()) {
//            projectsRoot.put(pro, prefix + projectsRoot.get(pro));
//        }

        for (String pro : projects) {
            retriveLogger(pro, projectsRoot, outputPath);
        }
    }
}
