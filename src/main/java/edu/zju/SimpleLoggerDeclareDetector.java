package edu.zju;

import com.google.common.io.Files;
import edu.zju.util.CodeUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleLoggerDeclareDetector {

    static final Logger LOG = LoggerFactory.getLogger(SimpleLoggerDeclareDetector.class);

    public List<LoggerProfiler> retrieveLoggers(String projectName, String sourcePath, String sourceEncoding, String jarPath,
                                String dependency, String outputDir) {
        File root = new File(sourcePath);
        String[] classPathEntries = CodeUtil.valueToStringArray(dependency + ";" + jarPath);
        String[] sourcePathEntries = CodeUtil.valueToStringArray(sourcePath);
        String[] encodings = CodeUtil.valueToStringArray(sourceEncoding);
        List<LoggerProfiler> loggers = new ArrayList<>();


        LOG.info("start parsing project {}", projectName);

        for (File f : Files.fileTreeTraverser().preOrderTraversal(root)) {
            if (f.isFile() && !f.getAbsolutePath().toLowerCase().contains("test") &&
                    Files.getFileExtension(f.getAbsolutePath()).equals("java")) {
                final String filePath = f.getAbsolutePath();
                // log count, sum of log lines
                LOG.info("reading file {}", filePath);
                String src = null;
                try {
                    src = Files.toString(f, Charset.forName(encodings[0]));
                } catch (IOException e) {
                    LOG.error("I/O error when reading file {}", filePath);
                    e.printStackTrace();
                }
                if (src == null)
                    continue;
                final String source = src;
                final int sloc = CodeUtil.calculateNumberOfLines(src);
                ASTParser parser = ASTParser.newParser(AST.JLS10);
                parser.setResolveBindings(true);
                parser.setKind(ASTParser.K_COMPILATION_UNIT);
                parser.setBindingsRecovery(true);
                parser.setStatementsRecovery(true);
                Map options = JavaCore.getOptions();
                parser.setCompilerOptions(options);
                parser.setUnitName(f.getAbsolutePath());
                parser.setEnvironment(classPathEntries, sourcePathEntries, encodings, true);
                parser.setSource(source.toCharArray());
                // CompilationUnit is possible to be incomplete when there are syntax errors in source file

                try {
                    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                    LoggerDeclVisitor visitor = new LoggerDeclVisitor(source, filePath, cu);
                    cu.accept(visitor);
                    loggers.addAll(visitor.getResults());

                } catch (Exception e) {
                    System.out.println("Apply ASTVistor for\t" + f.getAbsolutePath() + "\tfail.");
                    e.printStackTrace();
                }
                LOG.trace("finish parsing file {}", filePath);
            }
        }
        LOG.info("finish parsing project {}", projectName);
        return loggers;
    }


    class LoggerDeclVisitor extends ASTVisitor {

        protected String source;
        String filePath;
        CompilationUnit cu;
        List<LoggerProfiler> results;
        String packageName = "(default)";
        List<String> importedClass = new ArrayList<>();

        public LoggerDeclVisitor(String source, String filePath, CompilationUnit cu) {
            this.source = source;
            this.filePath = filePath;
            this.cu = cu;
            results = new ArrayList<LoggerProfiler>();
        }

        public boolean visit(PackageDeclaration node) {
            packageName = node.getName().toString();
            return super.visit(node);
        }

        public boolean visit(ImportDeclaration node) {
            importedClass.add(node.getName().toString());
            return super.visit(node);
        }

        public boolean visit(MethodInvocation node) {
            String methodText = node.toString().trim().replace("\n", " ").replace("\t", " ");
            if (node.getName().toString().equals("getLogger")) {
                String varName = "";
                ASTNode parent = node.getParent();
                while (parent != null) {
                    if (parent instanceof VariableDeclarationFragment) {
                        varName = ((VariableDeclarationFragment) parent).getName().toString();
                    }
                    parent = parent.getParent();
                }

                String containerClass = packageName + ".";
                parent = node.getParent();
                while (parent != null) {
                    if (parent instanceof TypeDeclaration) {
                        containerClass = containerClass + ((TypeDeclaration) parent).getName().getFullyQualifiedName();
                        break;
                    }
                    if (parent instanceof AnonymousClassDeclaration) {
                        containerClass = containerClass + "Anonymous";
                        break;
                    }
                    if (parent instanceof EnumDeclaration) {
                        containerClass = containerClass + ((EnumDeclaration) parent).getName().getFullyQualifiedName();
                        break;
                    }
                    parent = parent.getParent();
                }

                String name = "";
                if (node.arguments().size() != 1) {
                    LOG.warn("the number of arguments in getLogger is not one!");
                }
                if (node.arguments().size() != 0) {
                    Object arg = node.arguments().get(0);
                    if (arg instanceof TypeLiteral) {
                        //TODO change to full qualified name, for most case, the TypeLiteral is current class
                        // However, there will be some exceptions. To handle these case, we get all imported class name,
                        // and match them to this TypeLiteral, if no one matches, then add current package name to the TypeLiteral
                        String typeName = ((TypeLiteral) arg).getType().toString();
                        if (containerClass.equals(packageName + "." + typeName)) {
                            name = containerClass;
                        } else {
                            boolean matched = false;
                            for (String imported : importedClass) {
                                if (imported.endsWith("." + typeName)) {
                                    name = imported;
                                    matched = true;
                                    break;
                                }
                            }
                            if (!matched) {
                                name = packageName + "." + typeName;
                            }
                        }
                    } else if (arg instanceof StringLiteral) {
                        name = ((StringLiteral) arg).getLiteralValue();
                    } else if (arg.toString().contains("getClass")) {
                        name = containerClass;
                    } else {
                        name = "Attention!!! " + arg.toString();
                        LOG.warn("The argument of getLogger is unknown type!");
                    }
                } else {
                    LOG.warn("The argument of getLogger is zero!");
                }

                parent = node.getParent();
                String scopeType = "";
                String container = "";
                while (parent != null) {
                    if (parent instanceof MethodDeclaration) {
                        scopeType = "method";
                        container = containerClass + "." + ((MethodDeclaration) parent).getName().getFullyQualifiedName() + "()";
                        break;
                    }
                    parent = parent.getParent();
                }
                if (scopeType.equals("method")) {
                    results.add(new LoggerProfiler(varName, name, scopeType, container, methodText, filePath));
                } else {
                    results.add(new LoggerProfiler(varName, name, "class", containerClass, methodText, filePath));
                }

                return false;
            }
            return true;
        }

        public List<LoggerProfiler> getResults() {
            return results;
        }

    }

    public static void main(String[] args) {

        SimpleLoggerDeclareDetector detector = new SimpleLoggerDeclareDetector();
        List<LoggerProfiler> loggers = detector.retrieveLoggers("storm", "E:/Code/tmp/storm",
                "UTF-8", "", "", "E:/Code/tmp/storm");
        for (LoggerProfiler logger : loggers) {
            System.out.println(logger.toString());
        }

    }

}
