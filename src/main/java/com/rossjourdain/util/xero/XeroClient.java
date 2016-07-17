
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.github.scribejava.core.model.OAuth1RequestToken;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.client.httpclient4.HttpClientPool;
import net.oauth.http.HttpResponseMessage;
import net.oauth.signature.RSA_SHA1;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Construct this class for the private API.
 *
 * For Public and Partner API use {@link PublicXeroClient}
 * @author ross
 */
public class XeroClient {

    private String consumerKey;
    private String consumerSecret;
    private String privateKey; // NOTE This is only for private apps

    private static final HttpClientPool SHARED_CLIENT = new SingleClient();

    /**
     * For use by subclasses.  i.e. Public and Partner apps
     */
    protected XeroClient(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public XeroClient(String consumerKey, String consumerSecret, String privateKey) {
        this(consumerKey,consumerSecret);
        this.privateKey = privateKey;
    }

    public XeroClient(XeroClientProperties clientProperties) {
        this.consumerKey = clientProperties.getConsumerKey();
        this.consumerSecret = clientProperties.getConsumerSecret();
        this.privateKey = clientProperties.getPrivateKey();
    }

    public String getEndpointUrl() {
        return "https://api.xero.com/api.xro/2.0/";
    }

    public String getRequestTokenUrl() {
        return "https://api.xero.com/oauth/RequestToken";
    }

    public String getAuthoriseUrl() {
        return "https://api.xero.com/oauth/Authorize";
    }

    public String getAccessTokenUrl() {
        return "https://api.xero.com/oauth/AccessToken";
    }

    protected OAuthAccessor buildAccessor() {
        return buildAccessor(null);
    }

    protected OAuthAccessor buildAccessor(String callbackUrl) {
        OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, null, getServiceProvider());
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = consumerKey;
        accessor.tokenSecret = consumerSecret;
        return accessor;
    }

    protected OAuthServiceProvider getServiceProvider() {
        return new OAuthServiceProvider(getRequestTokenUrl(), getAuthoriseUrl(), getAccessTokenUrl());
    }

    public OAuth1RequestToken getRequestToken(String callbackUrl) throws OAuthException, IOException, URISyntaxException {
        OAuthAccessor accessor = buildAccessor(callbackUrl);
        Collection<OAuth.Parameter> parameters = new ArrayList<>();
        //  Hmm, this seems stupid having to set it as a param (as it's in the accessor), but fails without it.
        parameters.add(new OAuth.Parameter("oauth_callback",accessor.consumer.callbackURL));
        getOAuthClient().getRequestToken(accessor,null,parameters);
        return new OAuth1RequestToken(accessor.requestToken,accessor.tokenSecret);
    }

    public ArrayOfInvoice getInvoices() throws XeroClientUnexpectedException, OAuthProblemException {
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Invoices", null);
            ArrayOfInvoice invoices = XeroXmlManager.fromXml(response.getBodyAsStream()).getInvoices();
            if ( invoices == null ) return new ArrayOfInvoice();
            return invoices;
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public Invoice getInvoice(String invoiceId) throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfInvoice arrayOfInvoice = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Invoices" + "/" + invoiceId, null);
            arrayOfInvoice = XeroXmlManager.fromXml(response.getBodyAsStream()).getInvoices();
            if ( arrayOfInvoice.getInvoice().size() != 1 ) {
                throw new XeroClientUnexpectedException("Should have had 1 invoice for id[" + invoiceId + "] - Instead have[" + arrayOfInvoice.getInvoice().size() + "]",new IllegalStateException());
            }
            return arrayOfInvoice.getInvoice().get(0);
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public Organisation getOrganisation() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfOrganisation arrayOfContact = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Organisation", null);
            arrayOfContact = XeroXmlManager.fromXml(response.getBodyAsStream()).getOrganisations();
            if ( arrayOfContact.getOrganisation().size() != 1 ) {
                throw new XeroClientUnexpectedException("Should have had 1 orgainisation Instead have[" + arrayOfContact.getOrganisation().size() + "]",new IllegalStateException());
            }
            return arrayOfContact.getOrganisation().get(0);
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    /**
     * Currently returns ALL transactions for the specified bank code, in date order (may require multiple internal API hits, but gets put together for return)
     */
    public ArrayOfBankTransaction getBankTransactions(String bankCode) throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfBankTransaction arrayOfBankTransaction = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            Collection<OAuth.Parameter> parameters = new ArrayList<>();
            parameters.add(new OAuth.Parameter("where","BankAccount.Code==\"" + bankCode + "\""));
            OAuthMessage response;
            ArrayOfBankTransaction bankTransactions;
            int pageNo = 0;
            do {
                pageNo++;
                response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "BankTransactions?order=Date&page=" + pageNo, parameters);
                bankTransactions = XeroXmlManager.fromXml(response.getBodyAsStream()).getBankTransactions();
                if ( bankTransactions == null || bankTransactions.getBankTransaction() == null || bankTransactions.getBankTransaction().isEmpty() ) break;
                if (arrayOfBankTransaction == null ) {
                    arrayOfBankTransaction = bankTransactions;
                } else {
                    arrayOfBankTransaction.getBankTransaction().addAll(bankTransactions.getBankTransaction());
                }
            } while (true);
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfBankTransaction;
    }

    public ArrayOfItem getItems() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfItem arrayOfItem = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Items", null);
            arrayOfItem = XeroXmlManager.fromXml(response.getBodyAsStream()).getItems();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfItem;
    }

    public Contact getContact(String contactId) throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfContact arrayOfContact = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Contacts" + "/" + contactId, null);
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
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Contacts", null);
            arrayOfContact = XeroXmlManager.fromXml(response.getBodyAsStream()).getContacts();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        if ( arrayOfContact == null ) return new ArrayOfContact();
        return arrayOfContact;
    }

    public ArrayOfAccount getAccounts() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfAccount arrayOfAccount = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Accounts", null);
            arrayOfAccount = XeroXmlManager.fromXml(response.getBodyAsStream()).getAccounts();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        if ( arrayOfAccount == null ) return new ArrayOfAccount();
        return arrayOfAccount;
    }

    public ArrayOfTrackingCategory getTrackingCategories() throws XeroClientUnexpectedException, OAuthProblemException {
        ArrayOfTrackingCategory arrayOfTrackingCategory = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "TrackingCategories", null);
            arrayOfTrackingCategory = XeroXmlManager.fromXml(response.getBodyAsStream()).getTrackingCategories();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        if ( arrayOfTrackingCategory == null ) return new ArrayOfTrackingCategory();
        return arrayOfTrackingCategory;
    }

     public Report getReport(String reportUrl) throws XeroClientUnexpectedException, OAuthProblemException {
        Report report = null;
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, getEndpointUrl() + "Reports" + reportUrl, null);
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
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, getEndpointUrl() + "Contacts", OAuth.newList("xml", contactsString));
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
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, getEndpointUrl() + "Invoices", OAuth.newList("xml", invoicesXml));
            return XeroXmlManager.fromXml(response.getBodyAsStream()).getInvoices();
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }


    public void postPayments(ArrayOfPayment arrayOfPayment) throws XeroClientUnexpectedException, OAuthProblemException {
        try {
            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();
            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, getEndpointUrl() + "Payments", OAuth.newList("xml", paymentsString));
        } catch (OAuthProblemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public File getInvoiceAsPdfFile(String invoiceId) throws XeroClientUnexpectedException, OAuthProblemException {
        File file = null;
        FileOutputStream out = null;
        try {
            file = new File("Invoice-" + invoiceId + ".pdf");
            out = new FileOutputStream(file);
            out.write(getInvoiceAsPdfByteArray(invoiceId));
        } catch (IOException e) {
            throw new XeroClientUnexpectedException("", e);
        } finally {
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

    public byte[] getInvoiceAsPdfByteArray(String invoiceId) throws XeroClientUnexpectedException, OAuthProblemException {

        File file = null;
        InputStream in = null;
        ByteArrayOutputStream out = null;

        try {

            OAuthClient client = getOAuthClient();
            OAuthAccessor accessor = buildAccessor();

            OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, getEndpointUrl() + "Invoices" + "/" + invoiceId, null);
            request.getHeaders().add(new OAuth.Parameter("Accept", "application/pdf"));
            OAuthResponseMessage response = client.access(request, ParameterStyle.BODY);

            if (response != null && response.getHttpResponse() != null && (response.getHttpResponse().getStatusCode() == HttpResponseMessage.STATUS_OK)) {
                in = response.getBodyAsStream();
                out = new ByteArrayOutputStream();

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
        return out.toByteArray();
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    private OAuthClient getOAuthClient() {
        HttpClient4 httpClient4 = new HttpClient4(getClientPool());
        OAuthClient oAuthClient = new OAuthClient(httpClient4);
        oAuthClient.getHttpParameters().put(net.oauth.http.HttpClient.READ_TIMEOUT, new Integer(5000));
        oAuthClient.getHttpParameters().put(net.oauth.http.HttpClient.CONNECT_TIMEOUT, new Integer(5000));
        return oAuthClient;
    }

    protected HttpClientPool getClientPool() {
        return SHARED_CLIENT;
    }

    // This is based on the HttpClient4 one used by the default constructor, but allowing overridding the con per route.
    // It's also been updated to use

    protected static SingleClient getSingleClient() {
        return new SingleClient();
    }

    private static class SingleClient implements HttpClientPool {

        private final CloseableHttpClient client;

        public PoolingHttpClientConnectionManager getClientConnectionManager() {
            return clientConnectionManager;
        }

        private final PoolingHttpClientConnectionManager clientConnectionManager;


        // Bit lost with all of this, but this is the only way I've found to override the ConnPerRoute problem I was having.
        SingleClient() {
            clientConnectionManager = new PoolingHttpClientConnectionManager(5000,TimeUnit.MILLISECONDS);
            clientConnectionManager.setDefaultMaxPerRoute(30);
            client = HttpClientBuilder.create().setConnectionManager(clientConnectionManager).build();
//            client = new DefaultHttpClient(clientConnectionManager);

//                if (!(clientConnectionManager instanceof ThreadSafeClientConnManager)) {
//                    HttpParams params = client.getParams();
//                    client = new DefaultHttpClient(
//                            new ThreadSafeClientConnManager(clientConnectionManager.getSchemeRegistry(),
//                                    5000,TimeUnit.MILLISECONDS,
//                                    new ConnPerRouteBean(20)
//                            ), params);
//                }
            }


            public void closeExpiredConnections() {
                clientConnectionManager.closeExpiredConnections();
            }

            public HttpClient getHttpClient(URL server) {
                // May as well clean up expired connections.  Make sure to be logging org.apache.http on debug to see them getting cleaned up
                // Turned this off, looks like they are meant to stay persistent for performance reasons.
//                clientConnectionManager.closeExpiredConnections();
                return client;
            }
        }
}
