
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpResponseMessage;
import net.oauth.signature.RSA_SHA1;
import org.apache.http.client.HttpClient;

/**
 *
 * @author ross
 */
public class XeroClient {

    private String endpointUrl;
    private String consumerKey;
    private String consumerSecret;
    private String privateKey;

    public XeroClient(String endpointUrl, String consumerKey, String consumerSecret, String privateKey) {
        this.endpointUrl = endpointUrl;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.privateKey = privateKey;
    }

    public XeroClient(XeroClientProperties clientProperties) {
        this.endpointUrl = clientProperties.getEndpointUrl();
        this.consumerKey = clientProperties.getConsumerKey();
        this.consumerSecret = clientProperties.getConsumerSecret();
        this.privateKey = clientProperties.getPrivateKey();
    }

    protected OAuthAccessor buildAccessor() {
        OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, null, null);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = consumerKey;
        accessor.tokenSecret = consumerSecret;

        return accessor;
    }    

    public ArrayOfInvoice getInvoices() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfInvoice arrayOfInvoices = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Invoices", null);
            arrayOfInvoices = XeroXmlManager.fromXml(response.getBodyAsStream()).getInvoices();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfInvoices;
    }

    public Contact getContact(String contactId) throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfContact arrayOfContact = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Contacts" + "/" + contactId, null);
            arrayOfContact = XeroXmlManager.fromXml(response.getBodyAsStream()).getContacts();
            if ( arrayOfContact.getContact().size() != 1 ) {
                throw new XeroClientUnexpectedException("Should have had 1 client for id[" + contactId + "] - Instead have[" + arrayOfContact.getContact().size() + "]",new IllegalStateException());
            }
            return arrayOfContact.getContact().get(0);
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public ArrayOfContact getContacts() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfContact arrayOfContact = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Contacts", null);
            arrayOfContact = XeroXmlManager.fromXml(response.getBodyAsStream()).getContacts();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfContact;
    }

    public ArrayOfAccount getAccounts() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfAccount arrayOfAccount = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Accounts", null);
            arrayOfAccount = XeroXmlManager.fromXml(response.getBodyAsStream()).getAccounts();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfAccount;
    }

    public ArrayOfTrackingCategory getTrackingCategories() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfTrackingCategory arrayOfTrackingCategory = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "TrackingCategories", null);
            arrayOfTrackingCategory = XeroXmlManager.fromXml(response.getBodyAsStream()).getTrackingCategories();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfTrackingCategory;
    }

     public Report getReport(String reportUrl) throws XeroClientUnexpectedException, OAuthProblemException {
        Report report = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Reports" + reportUrl, null);
            ResponseType responseType = XeroXmlManager.xmlToResponse(response.getBodyAsStream());
            if (responseType != null && responseType.getReports() != null
                    && responseType.getReports().getReport() != null && responseType.getReports().getReport().size() > 0) {
                report = responseType.getReports().getReport().get(0);
            }
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return report;
    }

    public void postContacts(ArrayOfContact arrayOfContact) throws XeroClientUnexpectedException, OAuthProblemException {
        try {
            String contactsString = XeroXmlManager.contactsToXml(arrayOfContact);
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Contacts", OAuth.newList("xml", contactsString));
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public Invoice postInvoice(Invoice invoice) throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfInvoice arrayOfInvoice = new ArrayOfInvoice();
        arrayOfInvoice.getInvoice().add(invoice);
        ArrayOfInvoice returnedInvoices = postInvoices(arrayOfInvoice);
        return returnedInvoices.getInvoice().get(0);
    }

    public ArrayOfInvoice postInvoices(ArrayOfInvoice arrayOfInvoices) throws XeroClientUnexpectedException, OAuthProblemException {
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            String invoicesXml = XeroXmlManager.invoicesToXml(arrayOfInvoices);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Invoices", OAuth.newList("xml", invoicesXml));
            return XeroXmlManager.fromXml(response.getBodyAsStream()).getInvoices();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    private OAuthClient getOAuthClient() {
        HttpClient4 httpClient4 = new HttpClient4();
        OAuthClient oAuthClient = new OAuthClient(httpClient4);
        oAuthClient.getHttpParameters().put(net.oauth.http.HttpClient.READ_TIMEOUT, new Integer(5000));
        oAuthClient.getHttpParameters().put(net.oauth.http.HttpClient.CONNECT_TIMEOUT, new Integer(5000));
        return oAuthClient;
    }

    public void postPayments(ArrayOfPayment arrayOfPayment) throws XeroClientUnexpectedException, OAuthProblemException {
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Payments", OAuth.newList("xml", paymentsString));
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public File getInvoiceAsPdf(String invoiceId) throws XeroClientUnexpectedException, OAuthProblemException {

        File file = null;
        InputStream in = null;
        FileOutputStream out = null;

        try {

            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();

            OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, endpointUrl + "Invoices" + "/" + invoiceId, null);
            request.getHeaders().add(new OAuth.Parameter("Accept", "application/pdf"));
            OAuthResponseMessage response = client.access(request, ParameterStyle.BODY);


            file = new File("Invoice-" + invoiceId + ".pdf");

            if (response != null && response.getHttpResponse() != null && (response.getHttpResponse().getStatusCode() == HttpResponseMessage.STATUS_OK)) {
                in = response.getBodyAsStream();
                out = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } else {
                throw response.toOAuthProblemException();
            }

        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
        return file;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
