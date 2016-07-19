package com.rossjourdain.util.xero;

import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;


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
	protected final AccessToken accessToken;

	public PublicXeroClient(String consumerKey, String consumerSecret, AccessToken accessToken) {
		super( consumerKey, consumerSecret);
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
	}

	@Override
    public RequestToken getRequestToken(String callbackUrl) throws OAuthException, IOException, URISyntaxException {
        OAuthAccessor accessor = buildAccessor(callbackUrl);
        Collection<OAuth.Parameter> parameters = new ArrayList<>();
        //  Hmm, this seems stupid having to set it as a param (as it's in the accessor), but fails without it.
        parameters.add(new OAuth.Parameter(OAuth.OAUTH_CALLBACK,accessor.consumer.callbackURL));
        getOAuthClient().getRequestToken(accessor,null,parameters);
        return new RequestToken(accessor.requestToken,accessor.tokenSecret);
    }

	@Override
    public AccessToken getAccessToken(RequestToken requestToken) throws OAuthException, IOException, URISyntaxException {
        OAuthAccessor accessor = buildAccessor();
        Collection<OAuth.Parameter> parameters = new ArrayList<>();
//        parameters.add(new OAuth.Parameter(OAuth.OAUTH_TOKEN, requestToken));
        parameters.add(new OAuth.Parameter(OAuth.OAUTH_VERIFIER, requestToken.verifier));
        OAuthMessage oAuthMessage = getOAuthClient().getAccessToken(accessor, null, parameters);
        int expiresIn = Integer.parseInt(oAuthMessage.getParameter("oauth_expires_in"));
        return new AccessToken(accessor.accessToken,accessor.tokenSecret,oAuthMessage.getParameter("oauth_session_handle"),expiresIn);
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
		accessor.accessToken = accessToken.accessToken;
		accessor.tokenSecret = accessToken.tokenSecret;
		return accessor;
	}

}
