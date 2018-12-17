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
import javax.xml.bind.annotation.XmlElements;
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
    "renderer",
    "appender",
    "categoryOrLogger",
    "root",
    "categoryFactory"
})
@XmlRootElement(name = "log4j:configuration")
public class Log4JConfiguration {

    @XmlAttribute(name = "xmlns:log4j")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlnsLog4J;
    @XmlAttribute(name = "threshold")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String threshold;
    @XmlAttribute(name = "debug")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String debug;
    protected List<Renderer> renderer;
    protected List<Appender> appender;
    @XmlElements({
        @XmlElement(name = "category", type = Category.class),
        @XmlElement(name = "logger", type = Logger.class)
    })
    protected List<Object> categoryOrLogger;
    protected Root root;
    protected CategoryFactory categoryFactory;

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
     * 获取threshold属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThreshold() {
        if (threshold == null) {
            return "null";
        } else {
            return threshold;
        }
    }

    /**
     * 设置threshold属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThreshold(String value) {
        this.threshold = value;
    }

    /**
     * 获取debug属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDebug() {
        if (debug == null) {
            return "null";
        } else {
            return debug;
        }
    }

    /**
     * 设置debug属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDebug(String value) {
        this.debug = value;
    }

    /**
     * Gets the value of the renderer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the renderer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRenderer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Renderer }
     * 
     * 
     */
    public List<Renderer> getRenderer() {
        if (renderer == null) {
            renderer = new ArrayList<Renderer>();
        }
        return this.renderer;
    }

    /**
     * Gets the value of the appender property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appender property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppender().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Appender }
     * 
     * 
     */
    public List<Appender> getAppender() {
        if (appender == null) {
            appender = new ArrayList<Appender>();
        }
        return this.appender;
    }

    /**
     * Gets the value of the categoryOrLogger property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the categoryOrLogger property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategoryOrLogger().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Category }
     * {@link Logger }
     * 
     * 
     */
    public List<Object> getCategoryOrLogger() {
        if (categoryOrLogger == null) {
            categoryOrLogger = new ArrayList<Object>();
        }
        return this.categoryOrLogger;
    }

    /**
     * 获取root属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Root }
     *     
     */
    public Root getRoot() {
        return root;
    }

    /**
     * 设置root属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Root }
     *     
     */
    public void setRoot(Root value) {
        this.root = value;
    }

    /**
     * 获取categoryFactory属性的值。
     * 
     * @return
     *     possible object is
     *     {@link CategoryFactory }
     *     
     */
    public CategoryFactory getCategoryFactory() {
        return categoryFactory;
    }

    /**
     * 设置categoryFactory属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link CategoryFactory }
     *     
     */
    public void setCategoryFactory(CategoryFactory value) {
        this.categoryFactory = value;
    }

}
