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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;


/**
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class EventHandler implements EventCallback {

    private final LinkedBlockingQueue<StatusChangeType> eventQueue;

    public EventHandler() {
	eventQueue = new LinkedBlockingQueue<StatusChangeType>();
    }

    public StatusChangeType next() {
	StatusChangeType handle = null;

	do {
	    try {
		handle = eventQueue.poll(30, TimeUnit.SECONDS);
	    } catch (InterruptedException ex) {
		return null;
	    }
	} while (handle == null);

	return handle;
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    try {
		StatusChangeType statusChange = new StatusChangeType();
		statusChange.setAction(eventType.getEventTypeIdentifier());
		statusChange.setConnectionHandle((ConnectionHandleType) eventData);

		eventQueue.put(statusChange);
	    } catch (InterruptedException ignore) {
	    }
	}
    }

}
