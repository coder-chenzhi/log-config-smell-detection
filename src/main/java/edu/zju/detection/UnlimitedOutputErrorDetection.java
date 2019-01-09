package edu.zju.detection;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.zju.line.LocationAwareElement;
import edu.zju.line.LocatorAwareDocumentFactory;
import edu.zju.line.MySAXReader;

public class UnlimitedOutputErrorDetection {
	public Map<String, Integer> detectUnlimitedOutput(String configValue,String formatValue,String libraryValue)
	{
		List<String> errorAppenderList=new ArrayList<>();
		Map<String,Integer> appednerMap=new HashMap<>();
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
	        	if(element.getName().trim().toLowerCase().equals("appenders")){
	        		for(Element appender:(List<Element>)element.elements()){
	        			if("randomaccessfile".equals(appender.getName().toLowerCase())||"file".equals(appender.getName().toLowerCase()))
//	        				errorAppenderList.add(appender.getName());
	        				appednerMap.put(appender.attributeValue("name"),((LocationAwareElement)appender).getLineNumber());
	        			else if("rollingfile".equals(appender.getName().toLowerCase())||"rollingrandomaccessfile".equals(appender.getName().toLowerCase())){
	        				boolean flag=false;
	        				for(Element subProperty:(List<Element>)appender.elements()){
	        					if("policies".equals(subProperty.getName().trim().toLowerCase()))
        						{
	        						for(Element subsubProperty:(List<Element>)subProperty.elements())
	        						{
	        							if(subsubProperty.getName().equals("SizeBasedTriggeringPolicy"))
	        								flag=true;
	        						}
        						}
	        				}
	        				if(!flag){
//	        					errorAppenderList.add(appender.getName());
	        					appednerMap.put(appender.attributeValue("name"),((LocationAwareElement)appender).getLineNumber());
	        				}
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
	        		String classValue=element.attributeValue("class");
	        		String appenderType =classValue.substring(classValue.lastIndexOf('.')+1);
	        		if("FileAppender".equals(appenderType)||"DailyRollingFileAppender".equals(appenderType))
//	        			errorAppenderList.add(element.getName());
	        			appednerMap.put(element.attributeValue("name"),((LocationAwareElement)element).getLineNumber());
	        	}
	        }
			}
			else
			{
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
						String classValue=element.attributeValue("class");
		        		String appenderType =classValue.substring(classValue.lastIndexOf('.')+1);
		        		if("FileAppender".equals(appenderType))
//		        			errorAppenderList.add(element.getName());
		        			appednerMap.put(element.attributeValue("name"),((LocationAwareElement)element).getLineNumber());
		        		else if("RollingFileAppender".equals(appenderType)){
		        			List<Element> appenderElements=element.elements();
		        			for(Element appender:appenderElements){
		        				if(appender.getName().equals("rollingPolicy")){
		        					if(appender.attributeValue("class").indexOf("SizeAndTimeBasedRollingPolicy")!=-1||
		        							appender.attributeValue("class").indexOf("TimeBasedRollingPolicy")!=-1){
		        						if(appender.element("totalSizeCap")==null)
//		        							errorAppenderList.add(element.attributeValue("name"));
		        							appednerMap.put(element.attributeValue("name"),((LocationAwareElement)element).getLineNumber());
		        					}
		        					else if(appender.attributeValue("class").indexOf("FixedWindowRollingPolicy")!=-1){
		        						for(Element tmpAppender:appenderElements){
		        							if("triggeringPolicy".equals(tmpAppender.getName())){
		        								if(tmpAppender.element("maxFileSize")==null)
//		        									errorAppenderList.add(element.attributeValue("name"));
		        									appednerMap.put(element.attributeValue("name"),((LocationAwareElement)element).getLineNumber());
		        							}
		        						}
		        					}
		        				}
		        			}
		        		}
					}
				}
			}
		}
		return appednerMap;
	}
}
