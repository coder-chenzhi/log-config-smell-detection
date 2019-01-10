package edu.zju.entity;

public class LoggerProfiler {
    private String varName; // the name of the logger's variable
    private String name; // the name of the logger
    private String scopeType; // global or local
    private String container; // class name or method name
    private String rawText;
    private String filePath;

    public LoggerProfiler(String varName, String name, String scopeType, String container, String rawText, String filePath) {
        this.varName = varName;
        this.name = name;
        this.scopeType = scopeType;
        this.container = container;
        this.rawText = rawText;
        this.filePath = filePath;
    }

    public String getVarName() {
        return varName;
    }

    public String getName() {
        return name;
    }

    public String getScopeType() {
        return scopeType;
    }

    public String getContainer() {
        return container;
    }

    public String getRawText() {
        return rawText;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("filePath: ").append(filePath).append("\t");
        sb.append("rawText: ").append(rawText).append("\t");
        sb.append("scopeType: ").append(scopeType).append("\t");
        sb.append("container: ").append(container).append("\t");
        sb.append("loggerName: ").append(name).append("\t");
        sb.append("varName: ").append(varName);
        return sb.toString();
    }
}
