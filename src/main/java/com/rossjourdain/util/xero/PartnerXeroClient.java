package com.rossjourdain.util.xero;

import com.github.scribejava.core.model.OAuth1RequestToken;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.client.httpclient4.HttpClientPool;
import net.oauth.signature.RSA_SHA1;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


/**
 * For the partner API use this class.
 * i.e.
 * <p>
 * <code>PartnerXeroClient.initialise(..)</code>
 * <code>XeroClient client = new PartnerXeroClient(...)</code>
 *
 */
public class PartnerXeroClient extends PublicXeroClient {

	private static KeyStore entrustStore;

	private static char[] keyStorePassword;

	private static String privateKey;

	private static HttpClientPool SHARED_CLIENT;

	/**
	 * You must initialise this first.
	 *
	 * @param keyStorePath the path to the entrust keystore
	 * @param thePrivateKey the self signed privite key that had its public key uploaded to xero for your app.
	 */
	public static void initialise(String keyStorePath, String alias, char[] theKeyStorePassword, String thePrivateKey)
			throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
		keyStorePassword =  theKeyStorePassword;
		entrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		try (FileInputStream fis = new FileInputStream(keyStorePath)) {
			entrustStore.load(fis, theKeyStorePassword);
		}
		privateKey = thePrivateKey;
	}

	public PartnerXeroClient(String consumerKey, String consumerSecret, String accessToken, String tokenSecret) {
		super(consumerKey, consumerSecret,accessToken,tokenSecret);
		if ( privateKey == null ) {
			throw new IllegalStateException("Must call initialise before construction");
		}
	}

	@Override
	public String getEndpointUrl() {
		return "https://api-partner.network.xero.com/api.xro/2.0/";
	}

	@Override
	public String getRequestTokenUrl() {
		return "https://api-partner.network.xero.com/oauth/RequestToken";
	}

	public String getAuthoriseUrl() {
        return "https://api.xero.com/oauth/Authorize";
    }

    public String getAccessTokenUrl() {
        return "https://api-partner.network.xero.com/oauth/AccessToken";
    }
	@Override
	protected OAuthAccessor buildAccessor() {
		return buildAccessor(null);
	}

	@Override
	protected OAuthAccessor buildAccessor(String callbackUrl) {
		OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, consumerSecret, getServiceProvider());
		consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
		consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = accessToken;
		accessor.tokenSecret = tokenSecret;
		return accessor;
	}

	@Override
	protected HttpClientPool getClientPool() {
		if ( SHARED_CLIENT == null ) {
			try {
				SHARED_CLIENT = new PartnerHttpClientPool(entrustStore, keyStorePassword);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return SHARED_CLIENT;
	}

	private static class PartnerHttpClientPool implements HttpClientPool {

	    private HttpClient httpClient;

	    PartnerHttpClientPool(KeyStore entrustStore, char[] password)
	        throws KeyManagementException,
				NoSuchAlgorithmException,
				KeyStoreException,
				UnrecoverableKeyException {

	        SSLContext sslcontext = SSLContexts.custom()
	            .loadKeyMaterial(entrustStore, password)
	            .build();
	        httpClient = HttpClients.custom()
	            .setSslcontext(sslcontext)
	            .build();
	    }

	    public HttpClient getHttpClient(URL server) {
	        return this.httpClient;
	    }
	}
}
