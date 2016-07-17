package com.rossjourdain.util.xero;


import com.github.scribejava.core.services.RSASha1SignatureService;
import com.github.scribejava.core.services.SignatureService;

import java.security.PrivateKey;

public class XeroPartnerApi extends XeroApi
{

    private final PrivateKey privateKey;

    public XeroPartnerApi(PrivateKey privateKey) {
        this.privateKey = privateKey;
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
