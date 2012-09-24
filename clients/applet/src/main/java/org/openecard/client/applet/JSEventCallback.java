/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.applet;

import generated.StatusChangeType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import netscape.javascript.JSObject;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.binding.javascript.JavaScriptBinding;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.module.status.StatusChangeRequest;
import org.openecard.client.control.module.status.StatusChangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class JSEventCallback {

    private static final Logger logger = LoggerFactory.getLogger(JSEventCallback.class);

    private final ApplicationHandler handler;
    private final JSObject jsObject;
    private JavaScriptBinding binding;

    private String jsAppletStartedCallback;
    private String jsMessageCallback;

    public JSEventCallback(ECardApplet applet, ApplicationHandler handler) {
	this.handler = handler;
	this.jsObject = JSObject.getWindow(applet);
	this.jsAppletStartedCallback = applet.getParameter("jsAppletStartedCallback");
	this.jsMessageCallback = applet.getParameter("jsMessageCallback");

	setupJSBinding(this.handler);
    }

    private void setupJSBinding(ApplicationHandler handler) {
	try {
	    binding = new JavaScriptBinding();
	    ControlInterface control = new ControlInterface(binding);
	    control.getListeners().addControlListener(handler);
	    control.start();
	} catch (Exception ex) {
	    logger.error("Exception", ex);
	}
    }

    public void sendMessage(String message) {
	if (this.jsMessageCallback == null) {
	    return;
	}

	try {
	    this.jsObject.eval(this.jsMessageCallback + "(" + message + ")");
	} catch (Exception ignore) {
	}
    }

    public Object[] handle(String id, Object[] data) {
	// FIXME: this is a workaround until the javascript binding supports StatusChangeRequests/Responses
	if (id.equals("statuschange")) {
	    ClientResponse clientResponse = this.handler.request(new StatusChangeRequest());
	    Object[] response = null;

	    if (clientResponse != null && clientResponse instanceof StatusChangeResponse) {
		StatusChangeType statusChangeType = ((StatusChangeResponse) clientResponse).getStatusChangeType();
		String event = toJSON(statusChangeType.getAction(), statusChangeType.getConnectionHandle());

		response = new Object[]{ event };
	    } else {
		response = new Object[]{};
	    }

	    return response;
	}

	return binding.handle(id, data);
    }

    /**
     * Notify the JavaScript library that the Applet is started.
     */
    public void notifyScript() {
	try {
	    this.jsObject.eval(this.jsAppletStartedCallback + "()");
	} catch (Exception ignore) {
	}
    }

    private String toJSON(String action, ConnectionHandleType cHandle) {
	String contextHandle = ByteUtils.toHexString(cHandle.getContextHandle());
	String ifdName = cHandle.getIFDName();
	String slotIndex = cHandle.getSlotIndex() != null ? cHandle.getSlotIndex().toString() : "";
	String cardType = cHandle.getRecognitionInfo() != null ? cHandle.getRecognitionInfo().getCardType() : null;

	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"").append("action").append("\"").append(":").append("\"").append(action).append("\"").append(",");
	sb.append("\"").append("id").append("\"").append(":").append("\"").append(makeId(ifdName)).append("\"").append(",");
	sb.append("\"").append("ifdName").append("\"").append(":").append("\"").append(ifdName).append("\"").append(",");
	sb.append("\"").append("cardType").append("\"").append(":").append("\"").append(cardType).append("\"").append(",");
	sb.append("\"").append("contextHandle").append("\"").append(":").append("\"").append(contextHandle).append("\"").append(",");
	sb.append("\"").append("slotIndex").append("\"").append(":").append("\"").append(slotIndex).append("\"");
	sb.append("}");

	return sb.toString();
    }

    private static String makeId(String input) {
	try {
	    MessageDigest md = MessageDigest.getInstance("SHA");
	    byte[] bytes = md.digest(input.getBytes());
	    return ByteUtils.toHexString(bytes);
	} catch (NoSuchAlgorithmException ex) {
	    return input.replaceAll(" ", "_");
	}
    }
}
