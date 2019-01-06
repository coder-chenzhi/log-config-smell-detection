package edu.zju.detection;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.management.modelmbean.XMLParseException;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
public class LogErrorDetection {
	public static void getMagicValueError()
	{
		
	}
	public static void Test() throws DocumentException
	{
		SAXReader reader = new SAXReader();
        Document document = reader.read("D:/Document/OSS/Storm-cluster.xml");
        Element root = document.getRootElement();
        System.out.println(root.getText());
//        for (Element element:(List<Element>)root.elements() ) {
//            System.out.println(element.getName());
//            System.out.println(element.attributeCount());
//            System.out.println(element.getTextTrim());
//            List<Element> attributeList = element.elements();
//            for (Element attr : attributeList) {
//                System.out.println(attr.getName() + ": "+attr.getTextTrim());
//                List<Attribute> list=attr.attributes();
//                for(Attribute tmp:list)
//                {
//                	System.out.println(tmp.getName() + ": "+tmp.getText());
//                }
//            }
//            System.out.println("------------------------");
//            // do something
//        }
	}
	public static void main(String[] args) throws DocumentException
	{
//		PropertyConfigurator.configure("D:\\Document\\OSS\\Ambari-log4j.properties");
////		DOMConfigurator.configure("D:\\Document\\OSS\\active-mq.xml");
//		//PropertyConfigurator.configure("D:\\Document\\OSS\\Cassandra-logback.xml");
//		
//		Enumeration<Appender> it = Logger.getRootLogger().getAllAppenders();
//		while(it.hasMoreElements())
//		{
//			System.out.println(it.nextElement());
//		}
//		System.out.println(1);
		Test();
	}
}
