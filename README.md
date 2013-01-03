 Copyright 2011 Ross Jourdain

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


Xero API private application access to Xero using Java
===

This is a simple project to show you how to use Java to access your Xero data via the Xero API.
This project uses the 2-legged OAuth approach (OAuth 1.1a),
referred to by the Xero API as Private Applications.

##Step 1:  Setup a Xero application

Follow the instructions here:
http://blog.xero.com/developer/api-overview/setup-an-application/#private-apps

##Step 2:  Add your private key, consumer key and secret

Put your private key into: privateKey.pem
Put your consumerKey and consumerSecret into: xeroApi.properties

The following keys are needed for Xero Private Application.

```
endpointUrl=https://api.xero.com/api.xro/2.0/
consumerKey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
consumerSecret=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
privateKeyFile=./privateKey.pem
```

##Step 3:  Get the Schemas

Start with the modified Xero API schemas here:
https://github.com/rossjourdain/XeroAPI-Schemas

They should be up to date but if not, you can get the latest Xero API schemas here:
https://github.com/XeroAPI/XeroAPI-Schemas

##Step 4:  git clone http://github.com/indykish/xero.git

##Step 5:  Add the schemas to src/main/resources/XeroSchemas/v2.00/

##Step 6:  When mvn compile is executed the XeroSchemas/v2.00 xsd files are converted to Java classes.

This project uses the following projects:

-- Maven JAXB2 Plugin - XML to Java Object Unmarshalling --
http://confluence.highsource.org/display/MJIIP/Maven+JAXB2+Plugin
http://confluence.highsource.org/display/MJIIP/User+Guide

-- Java OAuth - OAuth implementation --
http://code.google.com/p/oauth/

The maven-jaxb2-plugin plugin in pom.xml compiles XeroSchemas to Java classes.

```
<plugin>
			<groupId>org.jvnet.jaxb2.maven2</groupId>
			<artifactId>maven-jaxb2-plugin</artifactId>
			<executions>
				<execution>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
</plugin>
```

When the project is loaded in eclipse, if the maven-jaxb2 plugin isn't present it is downloaded automatically.

Xero Public Application
===

This is a simple project to show you how to use Java to access your Xero data via the Xero API.
This project uses the 3-legged OAuth approach (OAuth 1.1a),
referred to by the Xero API as Public Application.

##Step 1:  Setup a Xero application

Follow the instructions here:
http://blog.xero.com/developer/api-overview/setup-an-application/#public-apps

#Step 2:  Generate an OAuth access token (If you wish to test API access with Xero using standalone mode)

You can use the xerorails project to generate one.

'''
git clone https://github.com/indykish/xerorails.git

Follow the instructions to as seen in xerorails to generate an access_token.
'''

#Step 3:  Add your consumer key and secret

Put your consumerKey and consumerSecret, oauth_token, oauth_verifier, oauth_token_secret into: xeroApi.properties

The following keys are needed for Xero Public Application.

```
oauth_token=XXXXXXXXXXXXXXXXX
oauth_verifier=XXXXXX
oauth_token_secret=XXXXXXXXXXXXXXXXXXXXX
requestUrl=https://api.xero.com/oauth/RequestToken
authorizationUrl=https://api.xero.com/oauth/Authorize
accessUrl=https://api.xero.com/oauth/AccessToken
```

##Step 3:  Get the Schemas

Start with the modified Xero API schemas here:
https://github.com/rossjourdain/XeroAPI-Schemas

They should be up to date but if not, you can get the latest Xero API schemas here:
https://github.com/XeroAPI/XeroAPI-Schemas

##Step 4:  git clone http://github.com/indykish/xero.git

##Step 5:  Add the schemas to src/main/resources/XeroSchemas/v2.00/

##Step 6:  When mvn compile is executed the XeroSchemas/v2.00 xsd files are converted to Java classes.

##Step 7:  The class com.rossjourdain.util.XeroPublicClient should be used to interface with Xero.
