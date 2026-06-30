
package com.difisoft.nhsv.admin.domain.vn._1sms;

import javax.xml.bind.annotation.*;


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
 *         &lt;element name="SendMTResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sendMTResult"
})
@XmlRootElement(name = "SendMTResponse")
public class SendMTResponse {

    @XmlElement(name = "SendMTResult")
    protected String sendMTResult;

    /**
     * Gets the value of the sendMTResult property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSendMTResult() {
        return sendMTResult;
    }

    /**
     * Sets the value of the sendMTResult property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSendMTResult(String value) {
        this.sendMTResult = value;
    }

}
