package edu.zju.line;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

public class LocationAwareElement extends DefaultElement {

    private int lineNumber = -1;

    public LocationAwareElement(QName qname) {
        super(qname);
    }

    public LocationAwareElement(QName qname, int attributeCount) {
        super(qname, attributeCount);

    }

    public LocationAwareElement(String name, Namespace namespace) {
        super(name, namespace);

    }

    public LocationAwareElement(String name) {
        super(name);

    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
