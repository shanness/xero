package com.rossjourdain.util.xero;

public class AccessToken
{

    public final String accessToken;
    public final String tokenSecret;
    public final String sessionHandle;
    public final Integer expiresIn;

    /**
     * Only for use by the library
     */
    AccessToken(String accessToken, String tokenSecret, String sessionHandle, Integer expiresIn) {
        this.accessToken = accessToken;
        this.tokenSecret = tokenSecret;
        this.sessionHandle = sessionHandle;
        this.expiresIn = expiresIn;
    }

    /**
     * For partner apps (allows refreshing)
     */
    public AccessToken(String accessToken, String tokenSecret, String sessionHandle) {
        this.accessToken = accessToken;
        this.tokenSecret = tokenSecret;
        this.sessionHandle = sessionHandle;
        expiresIn = null;
    }

    /**
     * For public apps (refreshing not allowed)
     */
    public AccessToken(String accessToken, String tokenSecret) {
        this.accessToken = accessToken;
        this.tokenSecret = tokenSecret;
        sessionHandle = null;
        expiresIn = null;
    }
}
