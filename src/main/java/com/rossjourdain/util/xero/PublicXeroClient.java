package xero;

import com.rossjourdain.util.xero.ArrayOfInvoice;
import com.rossjourdain.util.xero.XeroClient;
import com.rossjourdain.util.xero.XeroClientException;
import com.rossjourdain.util.xero.XeroClientUnexpectedException;
import com.rossjourdain.util.xero.XeroXmlManager;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import org.scribe.model.Token;
import net.oauth.OAuthAccessor;

import java.util.List;

/**
 * FIXME : refactor the whole thing to use plays async WS. https://www.playframework.com/documentation/2.3.x/JavaOAuth
 */
public class PublicXeroClient extends XeroClient {

	private final String consumerKey;
	private final String consumerSecret;
	private final Token accessToken;

	public PublicXeroClient(String endpointUrl, String consumerKey, String consumerSecret, Token accessToken) {
		super(endpointUrl, consumerKey, consumerSecret, "");
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
