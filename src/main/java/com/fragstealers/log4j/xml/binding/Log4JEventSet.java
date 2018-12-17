//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.11 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2018.12.13 时间 07:04:06 PM CST 
//


package com.fragstealers.log4j.xml.binding;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "log4JEvent"
})
@XmlRootElement(name = "log4j:eventSet")
public class Log4JEventSet {

    @XmlAttribute(name = "xmlns:log4j")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlnsLog4J;
    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "includesLocationInfo")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String includesLocationInfo;
    @XmlElement(name = "log4j:event")
    protected List<Log4JEvent> log4JEvent;

    /**
     * 获取xmlnsLog4J属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlnsLog4J() {
        if (xmlnsLog4J == null) {
            return "http://jakarta.apache.org/log4j/";
        } else {
            return xmlnsLog4J;
        }
    }

    /**
     * 设置xmlnsLog4J属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlnsLog4J(String value) {
        this.xmlnsLog4J = value;
    }

    /**
     * 获取version属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.2";
        } else {
            return version;
        }
    }

    /**
     * 设置version属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * 获取includesLocationInfo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncludesLocationInfo() {
        if (includesLocationInfo == null) {
            return "true";
        } else {
            return includesLocationInfo;
        }
    }

    /**
     * 设置includesLocationInfo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludesLocationInfo(String value) {
        this.includesLocationInfo = value;
    }

    /**
     * Gets the value of the log4JEvent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the log4JEvent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLog4JEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Log4JEvent }
     * 
     * 
     */
    public List<Log4JEvent> getLog4JEvent() {
        if (log4JEvent == null) {
            log4JEvent = new ArrayList<Log4JEvent>();
        }
        return this.log4JEvent;
    }

}
