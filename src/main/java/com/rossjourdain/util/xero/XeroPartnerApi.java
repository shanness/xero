package com.rossjourdain.util.xero;


import com.github.scribejava.core.services.RSASha1SignatureService;
import com.github.scribejava.core.services.SignatureService;
import org.apache.commons.codec.binary.Base64;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerValue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class XeroPartnerApi extends XeroApi
{

    private final PrivateKey privateKey;

    public XeroPartnerApi(String privateKeyString) {
        try {
            Base64 b64 = new Base64();
            byte [] decoded = b64.decode(privateKeyString);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SignatureService getSignatureService() {
        return new RSASha1SignatureService(privateKey);
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api-partner.network.xero.com/oauth/AccessToken";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://api-partner.network.xero.com/oauth/RequestToken";
    }
}
