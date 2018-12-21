package edu.zju;

import java.util.List;

import org.apache.commons.cli.*;

import edu.zju.util.DeadConfigurationErrorDetection;
import edu.zju.util.MagicValueErrorDetection;
import edu.zju.util.UnlimitedOutputErrorDetection;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();

        Option sourcePath = new Option("sp", "source", true, "source code root path");
        sourcePath.setRequired(true);
        options.addOption(sourcePath);

        Option configPath = new Option("cp", "config", true, "logging configuration file path");
        configPath.setRequired(true);
        options.addOption(configPath);

        Option format = new Option("f", "format", true, "file format of logging configuration, could be properties / xml");
        format.setRequired(true);
        options.addOption(format);

        Option library = new Option("l", "library", true, "logging libraries used by project, could be log4j / log4j2 / logback");
        library.setRequired(true);
        options.addOption(library);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("log config", options);

            System.exit(1);
            return;
        }

        String sourceValue = cmd.getOptionValue("source");
        String configValue = cmd.getOptionValue("config");
        String formatValue = cmd.getOptionValue("format");
        String libraryValue = cmd.getOptionValue("library");
        if(!"properties".equals(formatValue)&&!"xml".equals(formatValue))
        {
        	System.out.println("-f parameter error");
        	System.exit(1);
            return;
        }
        
        MagicValueErrorDetection magicValue=new MagicValueErrorDetection();
        List<String> ans=magicValue.detect(configValue,formatValue);
//        for(String s:ans)
//        	System.out.println(s);
//        DeadConfigurationErrorDetection dead=new DeadConfigurationErrorDetection();
//        List<String> deadAppenderList=dead.detectDeadAppender(sourceValue, configValue, formatValue, libraryValue);
//        List<String> deadLoggerList=dead.detectDeadLogger(sourceValue, configValue, formatValue, libraryValue);
//        
        
        UnlimitedOutputErrorDetection unlimitedDetect=new UnlimitedOutputErrorDetection();
        List<String> outlimitList=unlimitedDetect.detectUnlimitedOutput(configValue, formatValue, libraryValue);
    }

}
