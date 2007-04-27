//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.04.27 at 11:13:56 PM CEST 
//


package org.dhcpcluster.config.xml.data;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for stype-option-mode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="stype-option-mode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="replace"/>
 *     &lt;enumeration value="concat"/>
 *     &lt;enumeration value="regex"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum OptionMode {

    @XmlEnumValue("replace")
    REPLACE("replace"),
    @XmlEnumValue("concat")
    CONCAT("concat"),
    @XmlEnumValue("regex")
    REGEX("regex");
    private final String value;

    OptionMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OptionMode fromValue(String v) {
        for (OptionMode c: OptionMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
