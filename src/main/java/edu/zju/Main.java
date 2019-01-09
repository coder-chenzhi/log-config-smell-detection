package edu.zju;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.*;

import edu.zju.util.DeadConfigurationErrorDetection;
import edu.zju.util.MagicValueErrorDetection;
import edu.zju.util.UnlimitedOutputErrorDetection;
import edu.zju.entity.Location;

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
        Map<String,List<Location>> valueMap=magicValue.detect(configValue,formatValue);
//        for(String tmp:valueMap.keySet())
//        {
//        	System.out.printf("value:%-60s\t",addZeroForNum(tmp,60));
//        	List<Location> list=valueMap.get(tmp);
//        	System.out.printf("Times:%-3s\t",list.size());
//        	System.out.printf("Location:{");
//        	for(int i=0;i<list.size();i++){
//        		Location l=list.get(i);
//        		if(i==0)
//        			System.out.printf("%s",l.getDescribe()+":"+"line"+l.getLine());
//        		else
//        			System.out.printf(",%s",l.getDescribe()+":"+"line"+l.getLine());
//        	}
//        	System.out.println("}");
//        	//这里的还是没有进行判断的
//        }
        
        
//        DeadConfigurationErrorDetection dead=new DeadConfigurationErrorDetection();
//        Map<String, Integer> deadAppenderList=dead.detectDeadAppender(sourceValue, configValue, formatValue, libraryValue);
//        
//        Map<String, Integer> deadLoggerList=dead.detectDeadLogger(sourceValue, configValue, formatValue, libraryValue);
//        for(String tmp:deadAppenderList.keySet())
//        {
//        	System.out.printf("Unused appender: name: %-20s line: %s\n",addZeroForNum(tmp,20),deadAppenderList.get(tmp));
//        	
//        	//这里的还是没有进行判断的
//        }
//        System.out.println("-----------------------------");
//        for(String tmp:deadLoggerList.keySet())
//        {
//        	System.out.printf("Unused logger: name: %-20s line: %s\n",addZeroForNum(tmp,20),deadAppenderList.get(tmp));
//        }
//        
        
        UnlimitedOutputErrorDetection unlimitedDetect=new UnlimitedOutputErrorDetection();
        Map<String, Integer> outlimitList=unlimitedDetect.detectUnlimitedOutput(configValue, formatValue, libraryValue);
        for(String tmp:outlimitList.keySet())
	      {
	      	System.out.printf("Unlimited Appender: name: %-20s line: %s\n",addZeroForNum(tmp,20),outlimitList.get(tmp));
	      }
    }
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append(str).append(" ");// 左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
     
        return str;
    }
 
}

