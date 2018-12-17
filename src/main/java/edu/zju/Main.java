package edu.zju;

import org.apache.commons.cli.*;

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

        Option library = new Option(";l", "library", true, "logging libraries used by project, could be log4j / log4j2 / logback");
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

    }

}
