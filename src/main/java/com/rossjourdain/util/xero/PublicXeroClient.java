package com.rossjourdain.util.xero;

import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;


/**
 * FIXME : refactor the whole thing to use plays async WS. https://www.playframework.com/documentation/2.3.x/JavaOAuth
 *
 * For the public and partner API use this class.
 * i.e.
 * <p>
 * <code>Xero client = new PublicXeroClient(...)</code>
 *
 */
public class PublicXeroClient extends XeroClient {

	protected final String consumerKey;
	protected final String consumerSecret;
	protected final String accessToken;
	protected final String tokenSecret;

	public PublicXeroClient(String consumerKey, String consumerSecret, String accessToken, String tokenSecret) {
		super( consumerKey, consumerSecret);
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.tokenSecret = tokenSecret;
	}

	@Override
	protected OAuthAccessor buildAccessor() {
		return buildAccessor(null);
	}

	@Override
	protected OAuthAccessor buildAccessor(String callbackUrl) {
		OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, consumerSecret, getServiceProvider());
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
		accessor.accessToken = accessToken;
		accessor.tokenSecret = tokenSecret;
		return accessor;
	}

}
