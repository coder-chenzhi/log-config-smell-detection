package edu.zju.wala;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
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
import com.ibm.wala.util.io.FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;


public class MethodInvocation {
    public static final Logger LOG = LoggerFactory.getLogger(MethodInvocation.class);

    public static String getClasspath(String project, String root) throws IOException {
        String classpathFile = "wala/{project}_classpath.txt".replace("{project}", project.toLowerCase());
        File classPath = new File(MethodInvocation.class.getClassLoader().getResource(classpathFile).getFile());
        return Files.toString(classPath, Charsets.UTF_8).replace("{project_root}", root).replace(System.lineSeparator(), File.pathSeparator);
    }

    public static void main(String[] args) throws Exception {
        // 1.create an analysis scope representing the source file application
//        File exFile = new FileProvider().getFile("wala/no_exclusion.txt");
        File exFile = new File(MethodInvocation.class.getClassLoader().getResource("wala/no_exclusion.txt").getFile());
        String classpath = getClasspath("cassandra", "E:/temp/apache-cassandra-3.11.3");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("E:\\Code\\EclipseCommitterWork\\testproject\\target\\testproject-1.0-SNAPSHOT-shaded.jar", exFile);
        // 2. build a class hierarchy, call graph, and system dependence graph
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        System.out.println(cha.getNumberOfClasses() + " classes");
        LOG.info(cha.getNumberOfClasses() + " classes");

        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        IClass clazz;
        while (classes.hasNext()) {
            clazz = classes.next();
            if (!clazz.getName().toString().toLowerCase().contains("alibaba")) {
                continue;
            }
            System.out.println("class:\t" + clazz);
            LOG.info("class:\t" + clazz);
            for(IMethod m: clazz.getDeclaredMethods()) {
                // skip abstract method
                if (m.isAbstract()) {
                    continue;
                }
                // no need to handle (static) field initialization code,
                // because all static initialization code are in <clinit> named method
                System.out.println("method:\t" + m);
                LOG.info("method:\t" + m);
                try {
                    IR ir = cache.getIR(m); // can be null
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod callee = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget()); // can be null
                            String className = callee.getDeclaringClass().getName().toString();
                            String methodName = callee.getName().toString();
                            // TODO add other libraries
                            if ("Lorg/slf4j/LoggerFactory".equals(className) && "getLogger".equals(methodName)) {
                                if (instruction.getNumberOfUses() != 0) {
                                    int varIndex = instruction.getUse(0);
                                    String loggerName = "Unknown";
                                    if (ir.getSymbolTable().isStringConstant(varIndex)) {
                                        loggerName = ir.getSymbolTable().getValueString(varIndex);
                                        LOG.info("Detected logger naming by string:\t" + loggerName);
                                    } else {
                                        // Every class literal will generate a SSALoadMetadataInstruction,
                                        // which will include the name of class literal.
                                        // SSALoadMetadataInstruction will return a variable,
                                        // which will be used by  getLogger() method.
                                        SSAInstruction defineInst = cache.getDefUse(ir).getDef(varIndex);
                                        if (defineInst instanceof SSALoadMetadataInstruction) {
                                            loggerName =
                                                     ((SSALoadMetadataInstruction) defineInst).getToken().toString();
                                            LOG.info("Detected logger naming by class literal:\t" + loggerName);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }catch (Throwable e){
                    System.out.println("Error while creating IR for method: " + m.getReference() + "\n"+ e);
                    LOG.error("Error while creating IR for method: " + m.getReference(), e);
                }
            }
        }
    }
}
