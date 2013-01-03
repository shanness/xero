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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
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
    private String callbackUrl = "https://www.xero.com/callback_url";
    
    private String consumerKey;
    private String consumerSecret;
    private String oauth_token;
    private String oauth_token_secret;
    private String oauth_token_verifier;
    
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
	
	 public ArrayOfInvoice getInvoices() throws XeroClientException, XeroClientUnexpectedException {
	        ArrayOfInvoice arrayOfInvoices = null;
	        try {
                Properties paramProps = new Properties();
	            paramProps.setProperty(OAuth.OAUTH_TOKEN,  oauth_token);
                paramProps.setProperty(OAuth.OAUTH_VERIFIER,oauth_token_verifier);
                OAuthMessage response = sendRequest(paramProps, "Invoice");
	            arrayOfInvoices = XeroXmlManager.xmlToInvoices(response.getBodyAsStream());	            
	        } catch (OAuthProblemException ex) {
	        	ex.printStackTrace();
	            throw new XeroClientException("Error getting invoices", ex);
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	            throw new XeroClientUnexpectedException("", ex);
	        }
	        return arrayOfInvoices;
	    }
	
	 
	public OAuthMessage sendRequest(Map map, String bizFunction) throws IOException,
    URISyntaxException, OAuthException    {
		String url = endpointUrl + bizFunction;
		
		List<Map.Entry> params = new ArrayList<Map.Entry>();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry p = (Map.Entry) it.next();
			params.add(new OAuth.Parameter((String)p.getKey(),
            (String)p.getValue()));
		}
	      
		OAuthClient client = new OAuthClient(new HttpClient4());
		return client.invoke(buildAccessor(), "GET",  url, params);
   }
	
}
