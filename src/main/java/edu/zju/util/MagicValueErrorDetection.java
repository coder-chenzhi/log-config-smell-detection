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

public class MagicValueErrorDetection {
	Map<String,Integer> valueMap;
	public List<String> detect(String configValue,String formatValue){
		valueMap=new HashMap<String,Integer>();
		List<String> ans=new ArrayList<>();
		SAXReader reader = new SAXReader();
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
        for(String value:valueMap.keySet()){
        	if(valueMap.get(value)>=0){
        		ans.add(value);
        	}
        }
        return ans;
	}
	private void analyseElement(Element element)
	{
		if(element!=null){
			if(!"".equals(element.getTextTrim())){
				valueMap.put(element.getTextTrim(), valueMap.getOrDefault(element.getTextTrim(), 0));
			}
			List<Attribute> attributeList=element.attributes();
			if(attributeList!=null&&attributeList.size()!=0){
				for(Attribute attribute:attributeList){
					valueMap.put(attribute.getText().trim(), valueMap.getOrDefault(attribute.getText().trim(), 0));
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
