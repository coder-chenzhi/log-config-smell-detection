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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "renderer")
public class Renderer {

    @XmlAttribute(name = "renderedClass", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String renderedClass;
    @XmlAttribute(name = "renderingClass", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String renderingClass;

    /**
     * 获取renderedClass属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRenderedClass() {
        return renderedClass;
    }

    /**
     * 设置renderedClass属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRenderedClass(String value) {
        this.renderedClass = value;
    }

    /**
     * 获取renderingClass属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRenderingClass() {
        return renderingClass;
    }

    /**
     * 设置renderingClass属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRenderingClass(String value) {
        this.renderingClass = value;
    }

}
