package com.rossjourdain.util.xero;

public class RequestToken
{

    final String verifier;
    public final String requestToken;
    public final String tokenSecret;

    /**
     * For returned request tokens, only constructed by the library
     */
    RequestToken(String requestToken, String tokenSecret) {
        this.requestToken = requestToken;
        this.tokenSecret = tokenSecret;
        verifier = null;
    }

    /**
     * For passing into getAccessToken after initial authorisation callback.
     */
    public RequestToken(String requestToken, String tokenSecret, String verifier) {
        this.requestToken = requestToken;
        this.tokenSecret = tokenSecret;
        this.verifier = verifier;
    }
}
