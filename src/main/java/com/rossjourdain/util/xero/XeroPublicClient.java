/* 
** Copyright [2012] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.rossjourdain.util.xero;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static com.rossjourdain.util.xero.Constants.*;

import net.oauth.OAuth;
import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
import net.oauth.client.httpclient4.*;

/**
 * @author pandiyaraja
 *
 */
public class XeroPublicClient {
	
	private String endpointUrl;
	private String reqUrl;
	private String authzUrl;
	private String accessUrl;	
    private String callbackUrl;
    
    private String consumerKey;
    private String consumerSecret;
    private String oauth_token;
    private String oauth_token_secret;
    private String oauth_token_verifier;
    private String privateKey;
    
    public XeroPublicClient(Map<String, String> args) {
        this.endpointUrl = ENDPOINT_URL;
        this.reqUrl=REQUEST_URL;
        this.authzUrl=AUTHRIZE_URL;
        this.accessUrl=ACCESS_URL;
        this.callbackUrl=CALL_BACK_URL;
        
        this.oauth_token=args.get(OAUTH_TOKEN);
        this.oauth_token_secret=args.get(OAUTH_SECRET);
        this.oauth_token_verifier=args.get(OAUTH_VERIFIER);
        
        this.consumerKey=args.get(CONSUMER_KEY);
        this.consumerSecret=args.get(CONSUMER_SECRET);
        
    }
    
    public XeroPublicClient(XeroClientProperties clientProperties) {
        endpointUrl = clientProperties.getEndpointUrl();       
        reqUrl = clientProperties.getRequestURL();
        authzUrl = clientProperties.getAuthURL();
        accessUrl = clientProperties.getAccessURL();
        
        consumerKey = clientProperties.getConsumerKey();
        consumerSecret = clientProperties.getConsumerSecret();

        oauth_token = clientProperties.getOAuthToken();
        oauth_token_secret = clientProperties.getOAuthTokenSecret();
        oauth_token_verifier = clientProperties.getOAuthTokenVerifier();
    }
    /**
     * This method builds OAuthAccessor and return to where this method
     * called. OAuthAccessor is a type of OAuthClient invoke method.        
     * @return
     */
	private OAuthAccessor buildAccessor(){	        
        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
       consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
       OAuthAccessor accessor = new OAuthAccessor(consumer);
       accessor.tokenSecret = oauth_token_secret;
       return accessor;
    } 
	/**
	 * This method returns a list of Xero items and its details. 
	 * It calls send request method to send request to OAuthClient,
	 * OAuth client returns an array of Xero by calling XeroXmlManager's 
	 * xmlToXero item method, that method reads body of response and returns 
	 * XML format output. 
	 * @param module
	 * @return
	 * @throws XeroClientException
	 * @throws XeroClientUnexpectedException
	 */
	 public String listAll(String module) throws XeroClientException, XeroClientUnexpectedException {
	        String responseString;
	        try {
                List<Parameter> getList = OAuth.newList(OAuth.OAUTH_TOKEN, oauth_token, OAuth.OAUTH_VERIFIER, oauth_token_verifier);
                responseString = sendRequest(getList, module, GET).readBodyAsString();
	        } catch (OAuthProblemException ex) {
	        	ex.printStackTrace();
	            throw new XeroClientException("Error getting invoices", ex);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	            throw new XeroClientUnexpectedException("", ex);
	        }
	        return responseString;
	    }
	 /**
	  * This method returns a particular Xero item details by getting
	  * invoiceId value, then it perform like list method.
	  * @param invoiceId
	  * @param module
	  * @return
	  * @throws XeroClientException
	  * @throws XeroClientUnexpectedException
	  */
	 public String list(String id, String module) throws XeroClientException, XeroClientUnexpectedException {
		 String response;
	        try {
                 List<Parameter> getList=OAuth.newList(OAuth.OAUTH_TOKEN, oauth_token, OAuth.OAUTH_VERIFIER, oauth_token_verifier);
                 response = sendRequest(getList, module+"/"+id, GET).readBodyAsString();
	        } catch (OAuthProblemException ex) {
	        	ex.printStackTrace();
	            throw new XeroClientException("Error getting invoices", ex);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	            throw new XeroClientUnexpectedException("", ex);
	        }
	        return response;
	    }
	 /**
	  * This method can do both create and update Xero item and
	  * it takes String data as input that has Xero item arguments.
	  * @param data
	  * @param module
	  * @throws XeroClientException
	  * @throws XeroClientUnexpectedException
	  */
	 public String post(String data, String module) throws XeroClientException, XeroClientUnexpectedException {
		 String response;
		 try {			 			 
             List<Parameter> postList=OAuth.newList(OAuth.OAUTH_TOKEN, oauth_token, OAuth.OAUTH_VERIFIER, oauth_token_verifier, XML, data);
             response = sendRequest(postList, module, POST).readBodyAsString();	            	            
	        } catch (OAuthProblemException ex) {	
	        	ex.printStackTrace();
	            throw new XeroClientException("Error getting invoices", ex);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	            throw new XeroClientUnexpectedException("", ex);
	        }	
		 return response;
	 }
	 
	 public Report getReport(String reportUrl) throws XeroClientException, XeroClientUnexpectedException {
	        Report report = null;
	        try {
	            OAuthClient client = new OAuthClient(new HttpClient4());
	            OAuthAccessor accessor = buildAccessor();
	            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Reports" + reportUrl, null);
	            ResponseType responseType = XeroXmlManager.xmlToResponse(response.getBodyAsStream());
	            if (responseType != null && responseType.getReports() != null
	                    && responseType.getReports().getReport() != null && responseType.getReports().getReport().size() > 0) {
	                report = responseType.getReports().getReport().get(0);
	            }
	        } catch (OAuthProblemException ex) {
	            throw new XeroClientException("Error getting invoices", ex);
	        } catch (Exception ex) {
	            throw new XeroClientUnexpectedException("", ex);
	        }
	        return report;
	    }
	 
	 public void postContacts(ArrayOfContact arrayOfContact) throws XeroClientException, XeroClientUnexpectedException {
	        try {
	            String contactsString = XeroXmlManager.contactsToXml(arrayOfContact);
	            OAuthClient client = new OAuthClient(new HttpClient4());
	            OAuthAccessor accessor = buildAccessor();
	            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Contacts", OAuth.newList("xml", contactsString));
	        } catch (OAuthProblemException ex) {
	            throw new XeroClientException("Error posting contancts", ex);
	        } catch (Exception ex) {
	            throw new XeroClientUnexpectedException("", ex);
	        }
	    }
	 
	 public void postPayments(ArrayOfPayment arrayOfPayment) throws XeroClientException, XeroClientUnexpectedException {
	        try {
	            OAuthClient client = new OAuthClient(new HttpClient4());
	            OAuthAccessor accessor = buildAccessor();
	            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
	            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Payments", OAuth.newList("xml", paymentsString));
	        } catch (OAuthProblemException ex) {
	            throw new XeroClientException("Error posting payments", ex);
	        } catch (Exception ex) {
	            throw new XeroClientUnexpectedException("", ex);
	        }
	    }
	 public File getInvoiceAsPdf(String invoiceId) throws XeroClientException, XeroClientUnexpectedException {

	        File file = null;
	        InputStream in = null;
	        FileOutputStream out = null;

	        try {

	            OAuthClient client = new OAuthClient(new HttpClient4());
	            OAuthAccessor accessor = buildAccessor();

	            OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, endpointUrl + "Invoices" + "/" + invoiceId, null);
	            request.getHeaders().add(new OAuth.Parameter("Accept", "application/pdf"));
	            OAuthResponseMessage response = client.access(request, ParameterStyle.BODY);


	            file = new File("Invoice-" + invoiceId + ".pdf");

	            if (response != null && response.getHttpResponse() != null && (response.getHttpResponse().getStatusCode() / 2) != 2) {
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
	            throw new XeroClientException("Error getting PDF of invoice " + invoiceId, ex);
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
	 /**
	  * This method returns OAuthClient invoke method response.
	  * Client invoke method call from this method.
	  * @param params
	  * @param bizFunction
	  * @param method
	  * @return
	  * @throws IOException
	  * @throws URISyntaxException
	  * @throws OAuthException
	  */
	public OAuthMessage sendRequest(List<Parameter> params, String bizFunction, String method) throws IOException,
    URISyntaxException, OAuthException    {
		String url = endpointUrl + bizFunction;	      
		OAuthClient client = new OAuthClient(new HttpClient4());
		return client.invoke(buildAccessor(), method, url, params);
   }
	
}
