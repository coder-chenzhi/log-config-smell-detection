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
import java.util.List;

public class ParseDOM {

    public static Element getElementByName(Element root, String name){
        if (name.toLowerCase().equals(root.getName().trim().toLowerCase())) {
            return root;
        }
        if (root.elements().size() == 0) {
            return null;
        }
        for(Element subElement : (List<Element>)root.elements()) {
            Element e = getElementByName(subElement, name);
            if (e != null) {
                return e;
            }
        }
        return null;
    }


    public static Element getElementContainsProperty(Element root, String property){
        if (root.elements().size() == 0) {
            return null;
        }
        for(Element subElement : (List<Element>)root.elements()) {
            if (property.toLowerCase().equals(subElement.getName().trim().toLowerCase())) {
                return root;
            }
        }
        for(Element subElement : (List<Element>)root.elements()) {
            Element e = getElementContainsProperty(subElement, property);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String configPath = "F:\\Dropbox\\Research\\OwnWork\\Log\\Config\\data\\config\\Ali\\diamond-server-logback.xml";
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
        try {
            Document document = reader.read(configPath);
            System.out.println(getElementByName(document.getRootElement(), "pattern"));
        } catch (DocumentException e) {
            System.out.println("Error occurs when parsing the xml file");
            e.printStackTrace();
        }
    }
}
