//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.12.30 at 08:16:40 AM CET 
//


package org.dhcpcluster.config.xml.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for type-node complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="type-node">
 *   &lt;complexContent>
 *     &lt;restriction base="{}type-node-subnet">
 *       &lt;all>
 *         &lt;element ref="{}filter" minOccurs="0"/>
 *         &lt;element name="lease" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element ref="{}options" minOccurs="0"/>
 *         &lt;element ref="{}sub-nodes" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "type-node")
@XmlSeeAlso({
    Node.class
})
public class TypeNode
    extends TypeNodeSubnet
{


}
