/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.control.module.tctoken;

import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import org.openecard.bouncycastle.crypto.tls.ProtocolVersion;
import org.openecard.bouncycastle.crypto.tls.TlsPSKIdentity;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.HttpRequestLineUtils;
import org.openecard.control.module.tctoken.gui.InsertCardUserConsent;
import org.openecard.crypto.tls.ClientCertDefaultTlsClient;
import org.openecard.crypto.tls.ClientCertPSKTlsClient;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.openecard.crypto.tls.TlsNoAuthentication;
import org.openecard.crypto.tls.TlsPSKIdentityImpl;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.openecard.transport.paos.PAOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Transport binding agnostic TCToken handler.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GenericTCTokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericTCTokenHandler.class);

    final CardStateMap cardStates;
    private final Dispatcher dispatcher;
    private final UserConsent gui;
    private CardRecognition reg;

    public GenericTCTokenHandler(CardStateMap cardStates, Dispatcher dispatcher, UserConsent gui, CardRecognition reg){
	this.cardStates = cardStates;
	this.dispatcher = dispatcher;
	this.gui = gui;
	this.reg = reg;
    }

    public TCTokenRequest parseTCTokenRequestURI(URI requestURI) throws UnsupportedEncodingException, MalformedURLException, TCTokenException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();
	String queryStr = requestURI.getRawQuery();
	Map<String, String> queries = HttpRequestLineUtils.transform(queryStr);

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    String v = next.getValue();

	    if (k.equals("tcTokenURL")) {
		if (v != null && !v.isEmpty()) {
		    TCTokenType token = TCTokenFactory.generateTCToken(new URL(v));
		    tcTokenRequest.setTCToken(token);
		} else {
		    throw new IllegalArgumentException("Malformed TCTokenURL");
		}

	    } else if (k.equals("ifdName")) {
		if (v != null && !v.isEmpty()) {
		    tcTokenRequest.setIFDName(v);
		} else {
		    throw new IllegalArgumentException("Malformed IFDName");
		}

	    } else if (k.equals("contextHandle")) {
		if (v != null && !v.isEmpty()) {
		    tcTokenRequest.setContextHandle(v);
		} else {
		    throw new IllegalArgumentException("Malformed ContextHandle");
		}

	    } else if (k.equals("slotIndex")) {
		if (v != null && !v.isEmpty()) {
		    tcTokenRequest.setSlotIndex(v);
		} else {
		    throw new IllegalArgumentException("Malformed SlotIndex");
		}
	    } else if (k.equals("cardType")) {
		if (v != null && !v.isEmpty()) {
		    tcTokenRequest.setCardType(v);
		} else {
		    throw new IllegalArgumentException("Malformed CardType");
		}
	    } else {
		logger.debug("Unknown query element: {}", k);
	    }
	}
	return tcTokenRequest;
    }

    /**
     * Get the first found handle of the given card type.
     *
     * @param type the card type to get the first handle for
     * @return Handle describing the given card type or null if none is present.
     */
    private ConnectionHandleType getFirstHandle(String type) {
	ConnectionHandleType conHandle = new ConnectionHandleType();
	ConnectionHandleType.RecognitionInfo rec = new ConnectionHandleType.RecognitionInfo();
	rec.setCardType(type);
	conHandle.setRecognitionInfo(rec);
	Set<CardStateEntry> entries;
	entries = cardStates.getMatchingEntries(conHandle);
	if (entries.isEmpty()) {
	    InsertCardUserConsent uc = new InsertCardUserConsent(gui, reg, conHandle, cardStates);
	    return uc.show();
	} else {
	    return entries.iterator().next().handleCopy();
	}
    }

    private TCTokenResponse doPAOS(TCTokenType token, ConnectionHandleType connectionHandle) throws WSException, Exception {
	// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	cardApplicationPath.setCardAppPathRequest(connectionHandle);
	CardApplicationPathResponse cardApplicationPathResponse = (CardApplicationPathResponse) dispatcher
		.deliver(cardApplicationPath);

	// Check CardApplicationPathResponse
	WSHelper.checkResult(cardApplicationPathResponse);

	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		.getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse cardApplicationConnectResponse = (CardApplicationConnectResponse) dispatcher
		.deliver(cardApplicationConnect);
	// Update ConnectionHandle. It now includes a SlotHandle.
	connectionHandle = cardApplicationConnectResponse.getConnectionHandle();

	// Check CardApplicationConnectResponse
	WSHelper.checkResult(cardApplicationConnectResponse);

	try {
	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = new URL(token.getServerAddress());
	    String serverHost = serverAddress.getHost();
	    String secProto = token.getPathSecurityProtocol();

	    // Set up TLS connection
	    ClientCertTlsClient tlsClient;
	    if (secProto.equals("urn:ietf:rfc:4279") || secProto.equals("urn:ietf:rfc:5487")) {
		TlsNoAuthentication tlsAuth = new TlsNoAuthentication();
		tlsAuth.setHostname(serverHost);
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		byte[] psk = token.getPathSecurityParameters().getPSK();
		TlsPSKIdentity pskId = new TlsPSKIdentityImpl(sessionIdentifier.getBytes(), psk);
		tlsClient = new ClientCertPSKTlsClient(pskId, serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	    } else if (secProto.equals("urn:ietf:rfc:4346")) {
		TlsNoAuthentication tlsAuth = new TlsNoAuthentication();
		tlsAuth.setHostname(serverHost);
		// FIXME: verify certificate chain as soon as a usable solution exists fpr the trust problem
		//tlsAuth.setCertificateVerifier(new JavaSecVerifier());
		tlsClient = new ClientCertDefaultTlsClient(serverHost);
		tlsClient.setAuthentication(tlsAuth);
		tlsClient.setClientVersion(ProtocolVersion.TLSv11);
	    } else {
		throw new IOException("Unknow security protocol '" + secProto + "' requested.");
	    }

	    // TODO: remove this workaround as soon as eGK server uses HTTPS
	    if (serverAddress.getProtocol().equals("http")) {
		tlsClient = null;
	    }

	    // Set up PAOS connection
	    PAOS p = new PAOS(serverAddress, dispatcher, tlsClient);

	    // Send StartPAOS
	    StartPAOS sp = new StartPAOS();
	    sp.getConnectionHandle().add(connectionHandle);
	    sp.setSessionIdentifier(sessionIdentifier);
	    p.sendStartPAOS(sp);

	    TCTokenResponse response = new TCTokenResponse();
	    response.setRefreshAddress(new URL(token.getRefreshAddress()));
	    response.setResult(WSHelper.makeResultOK());

	    return response;
	} finally {
	    // disconnect card after authentication
	    CardApplicationDisconnect appDis = new CardApplicationDisconnect();
	    appDis.setConnectionHandle(connectionHandle);
	    dispatcher.deliver(appDis);
	}
    }

    /**
     * Activate the client.
     *
     * @param request ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    public TCTokenResponse handleActivate(TCTokenRequest request) {
	ConnectionHandleType connectionHandle = null;
	TCTokenResponse response = new TCTokenResponse();

	byte[] requestedContextHandle = request.getContextHandle();
	String ifdName = request.getIFDName();
	BigInteger requestedSlotIndex = request.getSlotIndex();

	if (requestedContextHandle == null || ifdName == null || requestedSlotIndex == null) {
	    // use dumb activation without explicitly specifying the card and terminal
	    // see TR-03112-7 v 1.1.2 (2012-02-28) sec. 3.2
	    connectionHandle = getFirstHandle(request.getCardType());
	} else {
	    // we know exactly which card we want
	    ConnectionHandleType requestedHandle = new ConnectionHandleType();
	    requestedHandle.setContextHandle(requestedContextHandle);
	    requestedHandle.setIFDName(ifdName);
	    requestedHandle.setSlotIndex(requestedSlotIndex);

	    Set<CardStateEntry> matchingHandles = cardStates.getMatchingEntries(requestedHandle);

	    if (!matchingHandles.isEmpty()) {
		connectionHandle = matchingHandles.toArray(new CardStateEntry[] {})[0].handleCopy();
	    }
	}

	if (connectionHandle == null) {
	    String msg = "No card available for the given ConnectionHandle.";
	    logger.error(msg);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
	    return response;
	}

	try {
	    return doPAOS(request.getTCToken(), connectionHandle);
	} catch (WSException w) {
	    logger.error(w.getMessage(), w);
	    response.setResult(w.getResult());
	    return response;
	} catch (Exception w) {
	    logger.error(w.getMessage(), w);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, w.getMessage()));
	    return response;
	}
    }

}
