
package com.difisoft.nhsv.admin.domain.vn._1sms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="pass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="sms" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="senderName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="phone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="isFlash" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="isUnicode" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "user",
    "pass",
    "sms",
    "senderName",
    "phone",
    "isFlash",
    "isUnicode"
})
@XmlRootElement(name = "SendMT")
public class SendMT {

    protected String user;
    protected String pass;
    protected String sms;
    protected String senderName;
    protected String phone;
    protected boolean isFlash;
    protected boolean isUnicode;

    /**
     * Gets the value of the user property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the pass property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPass() {
        return pass;
    }

    /**
     * Sets the value of the pass property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPass(String value) {
        this.pass = value;
    }

    /**
     * Gets the value of the sms property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSms() {
        return sms;
    }

    /**
     * Sets the value of the sms property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSms(String value) {
        this.sms = value;
    }

    /**
     * Gets the value of the senderName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the value of the senderName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSenderName(String value) {
        this.senderName = value;
    }

    /**
     * Gets the value of the phone property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the isFlash property.
     *
     */
    public boolean isIsFlash() {
        return isFlash;
    }

    /**
     * Sets the value of the isFlash property.
     *
     */
    public void setIsFlash(boolean value) {
        this.isFlash = value;
    }

    /**
     * Gets the value of the isUnicode property.
     *
     */
    public boolean isIsUnicode() {
        return isUnicode;
    }

    /**
     * Sets the value of the isUnicode property.
     *
     */
    public void setIsUnicode(boolean value) {
        this.isUnicode = value;
    }

}
