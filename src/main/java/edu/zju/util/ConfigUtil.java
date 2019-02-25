package edu.zju.util;

import edu.zju.line.LocationAwareElement;
import edu.zju.line.LocatorAwareDocumentFactory;
import edu.zju.line.MySAXReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

public class ConfigUtil {

    private static Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

    public static Map<String, Integer> extractLoggerNames(String configPath, String format, String library) {
        Map<String,Integer> loggerMap = new HashMap<>();
        Document document = null;

        if("xml".equals(format.toLowerCase())){
            SAXReader reader = new MySAXReader();
            reader.setDocumentFactory(new LocatorAwareDocumentFactory());
            // to surpass lack of dtd
            reader.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    if (systemId.contains("dtd")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });
            if("log4j2".equals(library.toLowerCase())){
                // log4j2.xml
                try {
                    document = reader.read(configPath);
                } catch (DocumentException e) {
                    LOG.error("Fail to parse document", e);
                }
                Element root = document.getRootElement();
                for (Element element : (List<Element>)root.elements()){
                    if(element.getName().trim().toLowerCase().equals("loggers")){
                        for(Element logger : (List<Element>)element.elements()){
                            if(logger.attributeValue("name") != null)
                            {
                                loggerMap.put(logger.attributeValue("name"), ((LocationAwareElement)logger).getLineNumber());
                            }
                        }
                    }
                }
            }
            else if("log4j".equals(library.toLowerCase()) || "logback".equals(library.toLowerCase())){
                // log4j.xml and logback.xml
                try {
                    document = reader.read(configPath);
                } catch (DocumentException e) {
                    LOG.error("Fail to parse document", e);
                }
                Element root = document.getRootElement();
                for (Element element : (List<Element>)root.elements()){
                    if(element.getName().trim().toLowerCase().equals("logger")){
                        loggerMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
                    }
                }
            }
        }
        else{
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configPath);
                prop.load(input);
                if("log4j".equals(library.toLowerCase())){
                    // log4j.properties
                    for (String key : prop.stringPropertyNames()) {
                        if (key.startsWith("log4j.logger.")) {
                            loggerMap.put(key.replace("log4j.logger.", ""), -1);
                        }
                    }
                } else {
                    // log4j2.properties
                    for (String key : prop.stringPropertyNames()) {
                        if (key.startsWith("logger.") && key.endsWith(".name")) {
                            loggerMap.put(prop.getProperty(key), -1);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Fail to read properties file {}", configPath, e);
            }
        }
        return loggerMap;
    }

    public static void main(String[] args) {
        String configRoot = "F:\\Dropbox\\Research\\OwnWork\\Log\\Config\\data\\config\\";
        String[] projects = new String[]{"OSS\\Activemq-log4j.properties", "OSS\\Ambari-log4j.properties",
                "OSS\\Cassandra-logback.xml", "OSS\\flume-log4j.properties", "OSS\\Hadoop-log4j.properties",
                "OSS\\HBase-log4j.properties", "OSS\\hive-log4j2.properties", "OSS\\Lucene-log4j2.xml",
                "OSS\\Storm-cluster-log4j2.xml", "OSS\\Zookeeper-log4j.properties", "Ali\\auctionplatform-log4j.xml",
                "Ali\\diamond-server-logback.xml", "Ali\\itemcenter-logback.xml", "Ali\\jingwei3_inner-logback.xml",
                "Ali\\notify1-logback.xml", "Ali\\tddl5-logback.xml", "Ali\\tlog-log4j.xml", "Ali\\tradeplatform-log4j.xml",
                "Ali\\fundplatform_conf\\fundplatform-log4j2.xml", "Ali\\buy2_conf\\logback-biz-logback.xml",
                "Ali\\buy2_conf\\logback-domain-logback.xml", "Ali\\buy2_conf\\logback-forest-logback.xml",
                "Ali\\buy2_conf\\logback-logback.xml", "Ali\\buy2_conf\\logback-misc-logback.xml",
                "Ali\\buy2_conf\\logback-monitor-logback.xml", "Ali\\buy2_conf\\logback-structure-logback.xml"
        };
//        projects = new String[]{"OSS\\Activemq-log4j.properties"};
        for (String pro : projects) {
            LOG.info("Parse {}", pro);
            String format = pro.substring(pro.lastIndexOf(".") + 1);
            String library = pro.substring(pro.lastIndexOf("-") + 1, pro.lastIndexOf("."));
            Map<String, Integer> loggers = extractLoggerNames(configRoot + pro, format, library);
            for (String logger : loggers.keySet()) {
                System.out.println(logger);
            }
        }
    }

}
