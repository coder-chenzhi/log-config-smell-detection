package edu.zju.line;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.Locator;


public class LocatorAwareDocumentFactory extends DocumentFactory {

    private Locator locator;

    public LocatorAwareDocumentFactory() {
        super();
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public Element createElement(QName qname) {
        LocationAwareElement element = new LocationAwareElement(qname);
        if (locator != null)
            element.setLineNumber(locator.getLineNumber());
        return (Element) element;
    }

}