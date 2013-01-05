
/*
 *  Copyright 2011 Ross Jourdain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.rossjourdain.util.xero;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import net.oauth.OAuthProblemException;

/**
 *
 * @author ross
 */
public class XeroXmlManager {
    /**
     * This method converts a given Xml InputStream to ResponseType
     * this method unmarshall the given InputStream.  
     * @param inputStream
     * @return
     */
    public static ResponseType fromXml(InputStream inputStream) {
        ResponseType response = null;
        try {
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ResponseType> element = unmarshaller.unmarshal(new StreamSource(inputStream), ResponseType.class);
            response = element.getValue();
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return response;
    }
    /**
     * 
     * @param responseStream
     * @return
     */
    public static ResponseType xmlToResponse(InputStream responseStream) {

        ResponseType response = null;

        try {
            JAXBContext context = JAXBContext.newInstance(ResponseType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ResponseType> element = unmarshaller.unmarshal(new StreamSource(responseStream), ResponseType.class);
            response = element.getValue();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return response;
    }
    /**
     * This method returns exceptipon from xero
     * @param exceptionString
     * @return
     */
    public static ApiExceptionExtended xmlToException(String exceptionString) {

        ApiExceptionExtended apiException = null;

        try {
            JAXBContext context = JAXBContext.newInstance(ApiExceptionExtended.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<ApiExceptionExtended> element = unmarshaller.unmarshal(new StreamSource(new StringReader(exceptionString)), ApiExceptionExtended.class);
            apiException = element.getValue();

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return apiException;
    }

    public static String oAuthProblemExceptionToXml(OAuthProblemException authProblemException) {

        String oAuthProblemExceptionString = null;

        Map<String, Object> params = authProblemException.getParameters();
        for (String key : params.keySet()) {
            Object o = params.get(key);
            if (key.contains("ApiException")) {
                oAuthProblemExceptionString = key + "=" + o.toString();
            }
        }

        return oAuthProblemExceptionString;
    }
    /**
     * This method converts given contact input details to xml data 
     * by using JAXBElement.
     * @param arrayOfContacts
     * @return
     */
    public static String contactsToXml(ArrayOfContact arrayOfContacts) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfContact> element = factory.createContacts(arrayOfContacts);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given invoice input details to xml data 
     * by using JAXBElement.
     */
    public static String invoicesToXml(ArrayOfInvoice arrayOfInvoices) {

        String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfInvoice> element = factory.createInvoices(arrayOfInvoices);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given account input details to xml data 
     * by using JAXBElement.
     */
    public static String accountsToXml(ArrayOfAccount arrayOfAccount) {

        String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
        JAXBElement<ArrayOfAccount> element = factory.createAccounts(arrayOfAccount);
        invoicesString=XeroXmlManager.marshall(element);
        System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given payments input details to xml data 
     * by using JAXBElement. 
     * @param arrayOfPayment
     * @return
     */
    public static String paymentsToXml(ArrayOfPayment arrayOfPayment) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfPayment> element = factory.createPayments(arrayOfPayment);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given employees input details to xml data 
     * by using JAXBElement.
     * @param arrayOfEmployee
     * @return
     */
    public static String employeesToXml(ArrayOfEmployee arrayOfEmployee) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfEmployee> element = factory.createEmployees(arrayOfEmployee);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given receipts input details to xml data 
     * by using JAXBElement.
     * @param arrayOfReceipt
     * @return
     */
    public static String receiptsToXml(ArrayOfReceipt arrayOfReceipt) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfReceipt> element = factory.createReceipts(arrayOfReceipt);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given item input details to xml data 
     * by using JAXBElement.
     * @param arrayOfItem
     * @return
     */
    public static String itemsToXml(ArrayOfItem arrayOfItem) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfItem> element = factory.createItems(arrayOfItem);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method converts given user input details to xml data 
     * by using JAXBElement.
     * @param arrayOfUser
     * @return
     */
    public static String usersToXml(ArrayOfUser arrayOfUser) {

    	String invoicesString = null;
        ObjectFactory factory = new ObjectFactory();
		JAXBElement<ArrayOfUser> element = factory.createUsers(arrayOfUser);
		invoicesString=XeroXmlManager.marshall(element);
		System.out.println(invoicesString);
        return invoicesString;
    }
    /**
     * This method can used by the input data to xml conversion methods.
     * marshaller implemented in this method.
     * @param element
     * @return
     */
    public static String marshall(JAXBElement element) {
    	String xmlString = null;
    	JAXBContext context;
		try {
			context = JAXBContext.newInstance(ResponseType.class);
			Marshaller marshaller = context.createMarshaller();
			 marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
	    	StringWriter stringWriter = new StringWriter();
	        marshaller.marshal(element, stringWriter);
	        xmlString = stringWriter.toString();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        System.out.println(xmlString);
        return xmlString;
    }
}
