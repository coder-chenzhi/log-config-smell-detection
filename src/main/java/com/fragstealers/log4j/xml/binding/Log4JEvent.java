//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.11 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2018.12.13 时间 07:04:06 PM CST 
//


package com.fragstealers.log4j.xml.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "log4JMessage",
    "log4JNDC",
    "log4JThrowable",
    "log4JLocationInfo"
})
@XmlRootElement(name = "log4j:event")
public class Log4JEvent {

    @XmlAttribute(name = "logger", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String logger;
    @XmlAttribute(name = "level", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String level;
    @XmlAttribute(name = "thread", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String thread;
    @XmlAttribute(name = "timestamp", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String timestamp;
    @XmlElement(name = "log4j:message", required = true)
    protected String log4JMessage;
    @XmlElement(name = "log4j:NDC")
    protected String log4JNDC;
    @XmlElement(name = "log4j:throwable")
    protected String log4JThrowable;
    @XmlElement(name = "log4j:locationInfo")
    protected Log4JLocationInfo log4JLocationInfo;

    /**
     * 获取logger属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogger() {
        return logger;
    }

    /**
     * 设置logger属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogger(String value) {
        this.logger = value;
    }

    /**
     * 获取level属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLevel() {
        return level;
    }

    /**
     * 设置level属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLevel(String value) {
        this.level = value;
    }

    /**
     * 获取thread属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThread() {
        return thread;
    }

    /**
     * 设置thread属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThread(String value) {
        this.thread = value;
    }

    /**
     * 获取timestamp属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 设置timestamp属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    /**
     * 获取log4JMessage属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLog4JMessage() {
        return log4JMessage;
    }

    /**
     * 设置log4JMessage属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLog4JMessage(String value) {
        this.log4JMessage = value;
    }

    /**
     * 获取log4JNDC属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLog4JNDC() {
        return log4JNDC;
    }

    /**
     * 设置log4JNDC属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLog4JNDC(String value) {
        this.log4JNDC = value;
    }

    /**
     * 获取log4JThrowable属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLog4JThrowable() {
        return log4JThrowable;
    }

    /**
     * 设置log4JThrowable属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLog4JThrowable(String value) {
        this.log4JThrowable = value;
    }

    /**
     * 获取log4JLocationInfo属性的值。
     * 
     * @return
     *     possible object is
     *     {@link Log4JLocationInfo }
     *     
     */
    public Log4JLocationInfo getLog4JLocationInfo() {
        return log4JLocationInfo;
    }

    /**
     * 设置log4JLocationInfo属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link Log4JLocationInfo }
     *     
     */
    public void setLog4JLocationInfo(Log4JLocationInfo value) {
        this.log4JLocationInfo = value;
    }

}
