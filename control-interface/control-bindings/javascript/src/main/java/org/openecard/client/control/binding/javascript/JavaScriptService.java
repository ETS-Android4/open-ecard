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

package org.openecard.client.control.binding.javascript;

import org.openecard.client.control.binding.javascript.handler.ControlJavaScriptHandler;
import org.openecard.client.control.handler.ControlHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class JavaScriptService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptService.class);
    private final ControlHandlers handlers;
    private final Thread thread;

    public JavaScriptService(ControlHandlers handlers) {
	this.handlers = handlers;

	thread = new Thread(this, "Open-eCard Localhost-Binding");
	logger.debug("Starting JavaScriptBinding");
    }

    public Object[] handle(String id, Object[] data) {
	try {
	    for (ControlHandler h : handlers.getControlHandlers()) {
		if (id.equals(h.getID())) {
		    if (h instanceof ControlJavaScriptHandler) {
			return ((ControlJavaScriptHandler) h).handle(data);
		    } else {
			logger.error("Handler [{}] is not supported by the JavaScriptBinding");
		    }
		}
	    }
	    logger.error("Cannot find a handler for the ID [{}]", id);
	    return null;
	} catch (Exception e) {
	    logger.error("Exception", e);
	    return null;
	}
    }

    /**
     * Starts the server.
     */
    public void start() {
	thread.start();
    }

    /**
     * Interrupts the server.
     */
    public void interrupt() {
	try {
	    thread.interrupt();
	} catch (Exception ignore) {
	}
    }

    @Override
    public void run() {
	while (!Thread.interrupted()) {
	    //TODO
	}
    }

}
