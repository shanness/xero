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

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * @author pandiyaraja
 *
 */
public class XeroPublicClientTest {
    @Test
	public void testCase() throws IOException, XeroClientException, XeroClientUnexpectedException {
    	XeroClientProperties clientProperties = new XeroClientProperties();
        clientProperties.load(new FileInputStream("./xeroApi.properties"));          	
		XeroPublicClient pc=new XeroPublicClient(clientProperties);
		//System.out.println(XeroXmlManager.invoicesToXml(pc.getInvoices()));
		
	}
}
