package edu.zju.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ExtractAppenderInfo {

    public static void extract(String configValue, String formatValue, String libraryValue) {
        SAXReader reader = new SAXReader();
        // To suppress the dtd not found error
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
        Document document = null;
        if ("xml".equals(formatValue)) {
            if ("log4j2".equals(libraryValue)) {
                try {
                    document = reader.read(configValue);
                } catch (DocumentException e) {
                    System.out.println("Error occurs when parsing the xml file");
                    e.printStackTrace();
                }
                Element root = document.getRootElement();
                for (Element element : (List<Element>) root.elements()) {
                    if (element.getName().trim().toLowerCase().equals("appenders")) {
                        for (Element appender : (List<Element>) element.elements()) {
                            String appenderType = appender.getName();
                            String appenderName = appender.attributeValue("name").toString();
                            Boolean patternDetected = appender.toString().contains("%m"); // each layout should contain this pattern at least
                            Element pattern = ParseDOM.getElementByName(appender,"pattern");
                            if (pattern != null) {
                                System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern:" + pattern.getTextTrim());
                            } else {
                                if (patternDetected) {
                                    System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern: detected but not found");
                                } else {
                                    System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tno pattern");
                                }
                            }
                        }
                    }
                }
            } else if ("log4j".equals(libraryValue)) {
                try {
                    document = reader.read(configValue);
                } catch (DocumentException e) {
                    System.out.println("Error occurs when parsing the xml file");
                    e.printStackTrace();
                }
                Element root = document.getRootElement();
                for (Element element : (List<Element>) root.elements()) {
                    if (element.getName().trim().toLowerCase().equals("appender")) {
                        String appenderName = element.attributeValue("name");
                        String appenderType = element.attributeValue("class");
                        Element layout = ParseDOM.getElementByName(element,"layout");
                        if (layout != null) {
                            Boolean patternDetected = element.toString().contains("%m"); // each layout should contain this pattern at least
                            String pattern = null;
                            for (Element e : (List<Element>) layout.elements()) {
                                if (e.attributeValue("name") != null && "ConversionPattern".equals(e.attributeValue("name"))) {
                                    pattern = e.attributeValue("value");
                                    break;
                                }
                            }
                            if (pattern != null) {
                                System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern:" + pattern);
                            } else {
                                if (patternDetected) {
                                    System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern: detected but not found");
                                } else {
                                    System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tno pattern");
                                }
                            }
                        } else {
                            System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tno pattern");
                        }
                    }
                }
            } else {
                try {
                    document = reader.read(configValue);
                } catch (DocumentException e) {
                    System.out.println("Error occurs when parsing the xml file");
                    e.printStackTrace();
                }
                Element root = document.getRootElement();
                for (Element element : (List<Element>) root.elements()) {
                    if (element.getName().trim().toLowerCase().equals("appender")) {
                        String appenderName = element.attributeValue("name");
                        String appenderType = element.attributeValue("class");
                        Boolean patternDetected = element.toString().contains("%m"); // each layout should contain this pattern at least
                        Element pattern = ParseDOM.getElementByName(element,"pattern");
                        if (pattern != null) {
                            System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern:" + pattern.getTextTrim());
                        } else {
                            if (patternDetected) {
                                System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tpattern: detected but not found");
                            } else {
                                System.out.println("appenderName:" + appenderName + "\tappenderType:" + appenderType + "\tno pattern");
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("Only XML format are supported now.");
        }
    }
    public static void main(String[] args) {
        String configPath = "F:\\Dropbox\\Research\\OwnWork\\Log\\Config\\data\\config\\Ali\\fundplatform_conf\\fundplatform-log4j2.xml";
        extract(configPath, "xml","log4j2");
    }
}
