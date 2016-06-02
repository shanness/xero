package com.rossjourdain.util.xero;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.model.Verb;

/**
 * Xero API for scribe.
 * 
 * @author Ashley Schroder
 * 
 */
public class XeroApi extends DefaultApi10a {
	
	private static final String AUTHORIZATION_URL = "https://api.xero.com/oauth/Authorize?oauth_token=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://api.xero.com/oauth/AccessToken";
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "https://api.xero.com/oauth/RequestToken";
	}

	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}

	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return String.format(AUTHORIZATION_URL, requestToken.getToken());
	}

}
