/*
 * Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.transport.tls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;
import org.openecard.bouncycastle.util.encoders.Hex;


/**
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@Ignore //works only from inside the HS
public class TlsPSKTest {

    /**
     * Test TLS-PSK without RSA
     *
     * @throws IOException
     */
    @Test
    public void testPSK() throws IOException {
	URL url = new URL("https://ftei-vm-073.hs-coburg.de:8888/");
	String host = url.getHost();
	byte[] identity = new String("pskuser").getBytes();
	byte[] psk = Hex.decode("0e8b59a3bc6cf8097b0aeb453d558266");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);

	TlsClientSocketFactory tlsClientSocketFactory = new TlsClientSocketFactory(tlsClient);

	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	conn.setSSLSocketFactory(tlsClientSocketFactory);
	conn.connect();

	InputStream response = null;
	StringBuilder sb = new StringBuilder();
	try {
	    response = conn.getInputStream();
	    InputStreamReader isr = new InputStreamReader(response);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    response.close();
	}
	// Server will response with some infos, including chosen Ciphersuite
	Assert.assertTrue(sb.toString().contains("PSK"));
    }

    /**
     * Test TLS-RSA-PSK
     *
     * @throws IOException
     */
    @Ignore
    @Test
    public void testRSAPSK() throws IOException {

	URL url = new URL("https://ftei-vm-073.hs-coburg.de:4433/");
	String host = url.getHost();
	byte[] identity = "Client_identity".getBytes();
	byte[] psk = Hex.decode("1234");

	PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(new TlsPSKIdentityImpl(identity, psk), host);

	// TODO exchange the following 4 lines with socket stuff below
	Socket s = new Socket(host, url.getPort());
	TlsProtocolHandler handler = new TlsProtocolHandler(s.getInputStream(), s.getOutputStream());
	handler.connect(tlsClient);
	handler.close();

	/*
	 * TlsClientSocketFactory tlsClientSocketFactory = new
	 * TlsClientSocketFactory(tlsClient);
	 *
	 * HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	 * conn.setSSLSocketFactory(tlsClientSocketFactory); conn.connect();
	 *
	 * InputStream response = null; StringBuilder sb = new StringBuilder();
	 * try { response = conn.getInputStream(); InputStreamReader isr = new
	 * InputStreamReader(response); BufferedReader br = new
	 * BufferedReader(isr); String line; while((line = br.readLine()) !=
	 * null){ sb.append(line); sb.append("\n"); } } finally {
	 * response.close(); } // Server will response with some infos,
	 * including chosen Ciphersuite
	 * Assert.assertTrue(sb.toString().contains("PSK"));
	 */
    }

}
