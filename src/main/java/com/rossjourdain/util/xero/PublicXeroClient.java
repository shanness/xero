package com.rossjourdain.util.xero;

import net.oauth.OAuthConsumer;
import org.scribe.model.Token;
import net.oauth.OAuthAccessor;


/**
 * FIXME : refactor the whole thing to use plays async WS. https://www.playframework.com/documentation/2.3.x/JavaOAuth
 */
public class PublicXeroClient extends XeroClient {

	private final String consumerKey;
	private final String consumerSecret;
	private final Token accessToken;

	public PublicXeroClient(String endpointUrl, String consumerKey, String consumerSecret, Token accessToken) {
		super(endpointUrl, consumerKey, consumerSecret, ""); // Last arg is private key, used for private apps
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
	}

	protected OAuthAccessor buildAccessor() {
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, consumerSecret, null);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = accessToken.getToken();
		accessor.tokenSecret = accessToken.getSecret();
		return accessor;
	}

}
