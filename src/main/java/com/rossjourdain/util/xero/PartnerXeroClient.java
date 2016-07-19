package com.rossjourdain.util.xero;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.client.httpclient4.HttpClientPool;
import net.oauth.signature.RSA_SHA1;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;


/**
 * For the partner API use this class.
 * i.e.
 * <p>
 *     Initialise once
 * <code>PartnerXeroClient.initialise(..)</code>
 * 	   And hit this for each call.
 * <code>XeroClient client = new PartnerXeroClient(...)</code>
 *
 */
public class PartnerXeroClient extends PublicXeroClient {

	private static KeyStore entrustStore;

	private static String privateKey;

	private static HttpClientPool SHARED_CLIENT;

	private static char[] keyStorePassword;

	/**
	 * You must initialise this first, before construction.
	 *
	 * @param theEntrustStore the loaded entrust keystore
	 * @param thePrivateKey the self signed privite key that had its public key uploaded to xero for your app.
	 */
	public static void initialise(KeyStore theEntrustStore, char[] theKeyStorePassword, String thePrivateKey)
			throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
		entrustStore = theEntrustStore;
		keyStorePassword = theKeyStorePassword;
		privateKey = thePrivateKey;
	}

	public PartnerXeroClient(String consumerKey, String consumerSecret, AccessToken accessToken) {
		super(consumerKey, consumerSecret,accessToken);
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
		accessor.accessToken = accessToken.accessToken;
		accessor.tokenSecret = accessToken.tokenSecret;
		return accessor;
	}

	@Override
    public AccessToken refreshAccessToken(AccessToken currentAccessToken) throws OAuthException, IOException, URISyntaxException {
        if ( currentAccessToken.sessionHandle == null ) {
            throw new OAuthException("Require a session handle.");
        }
        OAuthAccessor accessor = buildAccessor();
        Collection<OAuth.Parameter> parameters = new ArrayList<>();
        parameters.add(new OAuth.Parameter("oauth_session_handle", currentAccessToken.sessionHandle));
        OAuthMessage oAuthMessage = getOAuthClient().getAccessToken(accessor, null, parameters);
		int expiresIn = Integer.parseInt(oAuthMessage.getParameter("oauth_expires_in"));
		return new AccessToken(accessor.accessToken,accessor.tokenSecret,oAuthMessage.getParameter("oauth_session_handle"),expiresIn);
    }

	@Override
	protected HttpClientPool getClientPool() {
		if ( SHARED_CLIENT == null ) {
			try {
				SHARED_CLIENT = new PartnerHttpClientPool();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return SHARED_CLIENT;
	}

	private static class PartnerHttpClientPool implements HttpClientPool {

		private final PoolingHttpClientConnectionManager clientConnectionManager;

		private final CloseableHttpClient client;

		PartnerHttpClientPool() {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

				kmFactory.init(entrustStore, keyStorePassword);

				sslContext.init(kmFactory.getKeyManagers(), null, null);


//				SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext);
//
//				Scheme partnerHttps = new Scheme("https", sslSocketFactory, 443);
//				SchemeRegistry schemeRegistry = new SchemeRegistry();
//				schemeRegistry.register(partnerHttps);


				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext);

				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
						.register("https", sslConnectionFactory)
						.register("http", new PlainConnectionSocketFactory())
						.build();
//
				HttpClientBuilder builder = HttpClientBuilder.create();
				builder.setSSLSocketFactory(sslConnectionFactory);
				clientConnectionManager = new PoolingHttpClientConnectionManager(registry,null,null,null,5000, TimeUnit.MILLISECONDS);
				clientConnectionManager.setDefaultMaxPerRoute(30);
				builder.setConnectionManager(clientConnectionManager);

				client = builder.build();

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public HttpClient getHttpClient(URL url) {
			return client;
		}
	}
}
