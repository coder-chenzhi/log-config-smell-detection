package edu.zju.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.zju.line.LocatorAwareDocumentFactory;
import edu.zju.entity.Location;
import edu.zju.line.LocationAwareElement;
import edu.zju.line.MySAXReader;

public class MagicValueErrorDetection {
	Map<String,List<Location>> valueMap;
	public Map<String,List<Location>> detect(String configValue,String formatValue){
		valueMap=new HashMap<String,List<Location>>();
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
        try {
        	if("xml".equals(formatValue))
        		document = reader.read(configValue);
        	else
        		document =DocumentHelper.parseText(PropertiesToXmlUtil.getXml(configValue));
			analyseElement(document.getRootElement());
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			System.out.println("文档异常");
			e.printStackTrace();
		}
//        for(String value:valueMap.keySet()){
//        	if(valueMap.get(value)>=0){
//        		ans.add(value);
//        	}
//        }
        return valueMap;
	}
	private void analyseElement(Element element)
	{
		if(element!=null){
			if(!"".equals(element.getTextTrim())){
				List<Location> list=valueMap.getOrDefault(element.getTextTrim(), new ArrayList<Location>());
				list.add(new Location("text->"+element.getName(),((LocationAwareElement)element).getLineNumber()));
				valueMap.put(element.getTextTrim(), list);
			}
			List<Attribute> attributeList=element.attributes();
			if(attributeList!=null&&attributeList.size()!=0){
				for(Attribute attribute:attributeList){
					List<Location> list=valueMap.getOrDefault(attribute.getText().trim(), new ArrayList<Location>());
					list.add(new Location("Attribute->"+attribute.getName()+" of "+element.getName(),((LocationAwareElement)element).getLineNumber()));
					valueMap.put(attribute.getText().trim(), list);
				}
			}
			List<Element> elementList=element.elements();
			if(elementList!=null&&elementList.size()!=0){
				for(Element subElement:elementList){
					analyseElement(subElement);
				}
			}
		}
	}
}
