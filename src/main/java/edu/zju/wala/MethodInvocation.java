package edu.zju.wala;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
        return Files.toString(classPath, Charsets.UTF_8).replace("{project_root}", root).replace("\n", File.pathSeparator);
    }

    public static void main(String[] args) throws Exception {
        // 1.create an analysis scope representing the source file application
//        File exFile = new FileProvider().getFile("wala/no_exclusion.txt");
        File exFile = new File(MethodInvocation.class.getClassLoader().getResource("wala/no_exclusion.txt").getFile());
        String classpath = getClasspath("cassandra", "/home/chenzhi/Documents/apache-cassandra-3.11.3");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, exFile);
        // 2. build a class hierarchy, call graph, and system dependence graph
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        System.out.println(cha.getNumberOfClasses() + " classes");
        LOG.info(cha.getNumberOfClasses() + " classes");

        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        IClass clazz;
        while (classes.hasNext()) {
            clazz = classes.next();
            if (!clazz.getName().toString().toLowerCase().contains("cassandra")) {
                continue;
            }
            System.out.println("class:\t" + clazz);
            LOG.info("class:\t" + clazz);
            for(IMethod m: clazz.getDeclaredMethods()) {
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
                            if (className.equals("Lorg/slf4j/Logger")) {
                                LOG.info(callee.toString());
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
