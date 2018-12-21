package edu.zju.line;

import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.XMLReader;


public class MySAXReader extends SAXReader {

    @Override
    protected SAXContentHandler createContentHandler(XMLReader reader) {
        return new MySAXContentHandler(getDocumentFactory(),
                getDispatchHandler());
    }

    @Override
    public void setDocumentFactory(DocumentFactory documentFactory) {
        super.setDocumentFactory(documentFactory);
    }

}