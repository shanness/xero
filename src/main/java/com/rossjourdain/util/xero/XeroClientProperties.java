
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import net.oauth.OAuth;

/**
 *
 * @author ross
 */
public class XeroClientProperties extends Properties {

    private static final String ENDPOINT_URL = "endpointUrl";
    private static final String CONSUMER_KEY = "consumerKey";
    private static final String CONSUMER_SECRET = "consumerSecret";
    private static final String PRIVATE_KEY_FILE = "privateKeyFile";
    
    private static final String REQUEST_URL = "requestUrl";
    private static final String AUTH_URL = "authorizationUrl";
    private static final String ACCESS_URL = "accessUrl";
    
    
    private String endpointUrl;
    private String consumerKey;
    private String consumerSecret;
    private String privateKey;
	private String requestUrl;
	private String authUrl;
	private String accessUrl;
	private String oauth_token;
	private String oauth_token_secret;
	private String oauth_token_verifier;

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        loadMembers();
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        loadMembers();
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        super.loadFromXML(in);
        loadMembers();
    }

    private void loadMembers() throws FileNotFoundException, IOException {

        endpointUrl = getProperty(ENDPOINT_URL);
        consumerKey = getProperty(CONSUMER_KEY);
        consumerSecret = getProperty(CONSUMER_SECRET);
        
        oauth_token = getProperty(OAuth.OAUTH_TOKEN);
        oauth_token_verifier= getProperty(OAuth.OAUTH_VERIFIER);
        oauth_token_secret=getProperty(OAuth.OAUTH_TOKEN_SECRET);
        
        requestUrl = getProperty(REQUEST_URL);
        authUrl= getProperty(AUTH_URL);
        accessUrl=getProperty(ACCESS_URL);

        /*File file = new File(getProperty(PRIVATE_KEY_FILE));
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        privateKey = stringBuilder.toString();*/

    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getPrivateKey() {
        return privateKey;
    }

	/**
	 * @return
	 */
	public String getRequestURL() {
		return requestUrl;
	}

	/**
	 * @return
	 */
	public String getAuthURL() {
		return authUrl;
	}

	/**
	 * @return
	 */
	public String getAccessURL() {
		return accessUrl;
	}
	
	/**
	 * @return
	 */
	public String getOAuthToken() {
		return oauth_token;
	}

	/**
	 * @return
	 */
	public String getOAuthTokenSecret() {
		return oauth_token_secret;
	}

	/**
	 * @return
	 */
	public String getOAuthTokenVerifier() {
		return oauth_token_verifier;
	}
}