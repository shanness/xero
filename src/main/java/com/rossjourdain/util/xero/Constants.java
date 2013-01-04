/* 
** Copyright [2012] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.rossjourdain.util.xero;

/**
 * @author pandiyaraja
 *
 */
public class Constants {
    
	public static final String ENDPOINT_URL="https://api.xero.com/api.xro/2.0/";
	public static final String REQUEST_URL="https://api.xero.com/oauth/RequestToken";
	public static final String AUTHRIZE_URL="https://api.xero.com/oauth/Authorize";
	public static final String ACCESS_URL="https://api.xero.com/oauth/AccessToken";
	public static final String CALL_BACK_URL="https://www.xero.com/callback_url";
	
	public static final String OAUTH_TOKEN="oauth_token";
	public static final String OAUTH_SECRET="oauth_secret";
	public static final String OAUTH_VERIFIER="oauth_verifier";
	
	public static final String CONSUMER_KEY="consumer_key";
	public static final String CONSUMER_SECRET="consumer_secret";
	
	public static final String GET="GET";
	public static final String POST="POST";
	public static final String PUT="PUT";
	public static final String DELETE="DELETE";
	public static final String XML="xml";
}
