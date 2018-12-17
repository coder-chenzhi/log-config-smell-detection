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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "param",
    "priorityOrLevel",
    "appenderRef"
})
@XmlRootElement(name = "root")
public class Root {

    protected List<Param> param;
    @XmlElements({
        @XmlElement(name = "priority", type = Priority.class),
        @XmlElement(name = "level", type = Level.class)
    })
    protected List<Object> priorityOrLevel;
    @XmlElement(name = "appender-ref")
    protected List<AppenderRef> appenderRef;

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * 
     * 
     */
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

    /**
     * Gets the value of the priorityOrLevel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the priorityOrLevel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPriorityOrLevel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Priority }
     * {@link Level }
     * 
     * 
     */
    public List<Object> getPriorityOrLevel() {
        if (priorityOrLevel == null) {
            priorityOrLevel = new ArrayList<Object>();
        }
        return this.priorityOrLevel;
    }

    /**
     * Gets the value of the appenderRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the appenderRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAppenderRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AppenderRef }
     * 
     * 
     */
    public List<AppenderRef> getAppenderRef() {
        if (appenderRef == null) {
            appenderRef = new ArrayList<AppenderRef>();
        }
        return this.appenderRef;
    }

}
