package edu.zju.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.zju.LoggerProfiler;
import edu.zju.SimpleLoggerDeclareDetector;
import edu.zju.line.LocationAwareElement;
import edu.zju.line.LocatorAwareDocumentFactory;
import edu.zju.line.MySAXReader;

public class DeadConfigurationErrorDetection {
	public Map<String,Integer> detectDeadAppender(
			 String sourceValue
			,String configValue
			,String formatValue
			,String libraryValue){
		Set<String> appenderSet=new HashSet<>();
		Set<String> loggerSet=new HashSet<>();
		Map<String,Integer> appenderMap=new HashMap<>();
		SAXReader reader = new MySAXReader();
		reader.setDocumentFactory(new LocatorAwareDocumentFactory());
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
		Document document=null;
		if("xml".equals(formatValue)){
			if("log4j2".equals(libraryValue)){
		        try {
		        		document = reader.read(configValue);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					System.out.println("文档异常");
					e.printStackTrace();
				}
		        Element root=document.getRootElement();
		        //如果需要处理大小写的话那就还要加一个循环attribute
		        for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("appenders")){
		        		for(Element appender:(List<Element>)element.elements()){
		        			appenderSet.add(appender.attributeValue("name"));
		        			appenderMap.put(appender.attributeValue("name"), ((LocationAwareElement)appender).getLineNumber());
		        		}
		        	}
		        	if(element.getName().trim().toLowerCase().equals("loggers")||element.getName().trim().toLowerCase().equals("root")){
		        		for(Element logger:(List<Element>)element.elements()){
		        			for(Element ref:(List<Element>)logger.elements()){
		        				loggerSet.add(ref.attributeValue("ref"));
		        			}
		        		}
		        	}
		        }
		        
			}
			else if("log4j".equals(libraryValue)){
				 try {
		        		document = reader.read(configValue);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					System.out.println("文档异常");
					e.printStackTrace();
				}
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("appender")){
		        		appenderSet.add(element.attributeValue("name"));
		        		appenderMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
		        	}
		        	if(element.getName().trim().toLowerCase().equals("logger")||element.getName().trim().toLowerCase().equals("root")){
		        		for(Element logger:(List<Element>)element.elements()){
		        			if(logger.getName().toLowerCase().equals("appender-ref")||logger.getName().toLowerCase().equals("appenderref"))
		        				loggerSet.add(logger.attributeValue("ref"));
		        		}
		        	}
		        }
			}
			else{
				try {
	        		document = reader.read(configValue);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				System.out.println("文档异常");
				e.printStackTrace();
			}
				//logback和log4j处理是一样的
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("appender")){
		        		appenderSet.add(element.attributeValue("name"));
		        		appenderMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
		        		List<Element> elementsListOfAppender=element.elements();
		        		if(elementsListOfAppender!=null)
		        		{
		        			for(Element elementsOfAppender:elementsListOfAppender)
		        			{
		        				if(elementsOfAppender.getName().toLowerCase().equals("appender-ref"))
		        				{
		        					loggerSet.add(elementsOfAppender.attributeValue("ref"));
		        				}
		        			}
		        		}
		        	}
		        	if(element.getName().trim().toLowerCase().equals("logger")||element.getName().trim().toLowerCase().equals("root")){
		        		for(Element logger:(List<Element>)element.elements()){
		        			if(logger.getName().toLowerCase().equals("appender-ref")||logger.getName().toLowerCase().equals("appenderref"))
		        				loggerSet.add(logger.attributeValue("ref"));
		        		}
		        	}
		        }
			}
		}
		else
		{
			if("log4j".equals(libraryValue)){
				try {
					document =DocumentHelper.parseText(PropertiesToXmlUtil.getXml(configValue));
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					System.out.println("解析log4j的properties出错");
					e.printStackTrace();
				}
				//这里和上面的log4j.xml解析是一样的
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("appender")){
		        		appenderSet.add(element.attributeValue("name"));
		        		appenderMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
		        	}
		        	if(element.getName().trim().toLowerCase().equals("logger")||element.getName().trim().toLowerCase().equals("root")){
		        		for(Element logger:(List<Element>)element.elements()){
		        			if(logger.getName().toLowerCase().equals("appender-ref")||logger.getName().toLowerCase().equals("appenderref"))
		        				loggerSet.add(logger.attributeValue("ref"));
		        		}
		        	}
		        }
			}
			//这里还有一个log4j2的properties还没有写
		}
//		for(String tmp:appenderSet)
//			System.out.println(tmp);
//		System.out.println("--------------");
//		for(String tmp:loggerSet)
//			System.out.println(tmp);
		appenderSet.removeAll(loggerSet);
		for(String key:loggerSet)
			appenderMap.remove(key);
		return appenderMap;
	}
	public Map<String,Integer> detectDeadLogger(
			String sourceValue
			,String configValue
			,String formatValue
			,String libraryValue){
		Set<String> sourceLoggerSet=new HashSet<>();
		Set<String> loggerSet=new HashSet<>();
		Map<String,Integer> loggerMap=new HashMap<>();
		SAXReader reader = new MySAXReader();
		reader.setDocumentFactory(new LocatorAwareDocumentFactory());
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
		Document document=null;
		if("xml".equals(formatValue)){
			if("log4j2".equals(libraryValue)){
				try {
	        		document = reader.read(configValue);
				} catch (DocumentException e) {
				// TODO Auto-generated catch block
				System.out.println("文档异常");
				e.printStackTrace();
				}
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("loggers")){
		        		for(Element logger:(List<Element>)element.elements()){
		        			if(logger.attributeValue("name")!=null)
		        			{
		        				loggerSet.add(logger.attributeValue("name"));
		        				loggerMap.put(logger.attributeValue("name"), ((LocationAwareElement)logger).getLineNumber());
		        			}
		        		}
		        	}
		        }
			}
			else if("log4j".equals(libraryValue)||"logback".equals(libraryValue)){
				 try {
		        		document = reader.read(configValue);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					System.out.println("文档异常");
					e.printStackTrace();
				}
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("logger")){
		        		loggerSet.add(element.attributeValue("name"));
		        		loggerMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
		        	}
		        }
			}
		}
		else{
			if("log4j".equals(libraryValue)){
				try {
					document =DocumentHelper.parseText(PropertiesToXmlUtil.getXml(configValue));
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					System.out.println("解析log4j的properties出错");
					e.printStackTrace();
				}
				Element root=document.getRootElement();
				for (Element element:(List<Element>)root.elements()){
		        	if(element.getName().trim().toLowerCase().equals("logger")){
		        		loggerSet.add(element.attributeValue("name"));
		        		loggerMap.put(element.attributeValue("name"), ((LocationAwareElement)element).getLineNumber());
		        	}
		        }
			}
			//这里后面还有log4j2没有完成
		}
		SimpleLoggerDeclareDetector detector = new SimpleLoggerDeclareDetector();
        List<LoggerProfiler> loggers = detector.retrieveLoggers("source",sourceValue ,
                "UTF-8", "", "", sourceValue);
		for(LoggerProfiler logger:loggers)
			sourceLoggerSet.add(logger.getName());
		Set<String> removeSet=new HashSet<>();
		for(String logger:loggerSet)
		{
			for(String sourceLogger:sourceLoggerSet)
				if(sourceLogger.startsWith(logger))
					removeSet.add(logger);
		}
		loggerSet.removeAll(removeSet);
		for(String key:removeSet)
        	loggerMap.remove(key);
		
		return loggerMap;
	}
}
