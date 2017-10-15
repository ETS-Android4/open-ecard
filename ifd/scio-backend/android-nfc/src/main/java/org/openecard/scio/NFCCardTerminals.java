/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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

package org.openecard.scio;

import android.nfc.NfcAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.SCIOTerminals.State;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki
 * @author Daniel Nemmert
 * @author Mike Prechtl
 */
public class NFCCardTerminals implements SCIOTerminals {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminals.class);

    private final NfcAdapter adapter;

    public NFCCardTerminals(NfcAdapter adapter) {
	this.adapter = adapter;
    }

    @Override
    public List<SCIOTerminal> list(State arg0) throws SCIOException {
	List<SCIOTerminal> list = new ArrayList<SCIOTerminal>() {};
	for (Map.Entry<String, NFCCardTerminal> entry : NFCCardTerminal.getTerminals().entrySet()) {
	    list.add(entry.getValue());
	}
	return list;
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
        return list(State.ALL);
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
        return NFCCardTerminal.getInstance(name);
    }

    @Override
    public TerminalWatcher getWatcher() throws SCIOException {
	return new NFCCardWatcher(this);
    }

    private static class NFCCardWatcher implements TerminalWatcher {

        private NFCCardTerminals nfcTerminals;
        private NFCCardTerminal androidT;

        public NFCCardWatcher(NFCCardTerminals terminals) {
	    try {
		this.nfcTerminals = terminals;
		this.androidT = (NFCCardTerminal) terminals.getTerminal(NFCCardTerminal.STD_TERMINAL_NAME);
	    } catch (NoSuchTerminal ex) {
		LOG.error(ex.getMessage(), ex);
	    }
        }

        private Queue<StateChangeEvent> pendingEvents;
        private Collection<String> terminals;
        private Collection<String> cardPresent;

        private boolean isEnabled;

        @Override
        public SCIOTerminals getTerminals() {
            return nfcTerminals;
        }

        @Override
        public List<TerminalState> start() throws SCIOException {
	    LOG.debug("Entering start of nfc card watcher.");
            ArrayList<TerminalState> result = new ArrayList<>();
            if (pendingEvents != null) {
                throw new IllegalStateException("Trying to initialize already initialized watcher instance.");
            }
            pendingEvents = new LinkedList<>();
            terminals = new HashSet<>();
            cardPresent = new HashSet<>();

            // Check if NFC Adapter is present and enabled
            isEnabled = nfcTerminals.adapter.isEnabled();

            String name = androidT.getName();
            terminals.add(name);

            if (nfcTerminals.adapter == null) {
		String msg = "No nfc Adapter on this Android Device.";
                throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE);
            } else if (! isEnabled) {
                throw new SCIOException("Nfc Adapter not enabled.", SCIOErrorCode.SCARD_E_NO_SERVICE);
            } else if (nfcTerminals.adapter != null && isEnabled) {
		if (androidT.isCardPresent()) {
		    LOG.debug("Card is present.");
		    cardPresent.add(name);
		    result.add(new TerminalState(name, true));
		} else {
		    LOG.debug("No card is present.");
		    result.add(new TerminalState(name, false));
		}
            }

	    LOG.trace("Leaving start() with {} states.", result.size());
            return result;
        }

        @Override
        public StateChangeEvent waitForChange() throws SCIOException {
            return waitForChange(0);
        }

        @Override
        public StateChangeEvent waitForChange(long timeout) throws SCIOException {
	    LOG.debug("NFCCardWatcher wait for change...");
	    if (pendingEvents == null) {
		throw new IllegalStateException("Calling wait on uninitialized watcher instance.");
	    }

	    sleep(2500);

	    // try to return any present events first
	    StateChangeEvent nextEvent = pendingEvents.poll();
	    if (nextEvent != null) {
		LOG.trace("Leaving wait for change with queued event.");
		return nextEvent;
	    } else {
		Collection<String> newCardPresent = new HashSet<>();
		String ifdName = androidT.getName();
		if (androidT.isCardPresent()) {
		    LOG.debug("Card is present.");
		    newCardPresent.add(ifdName);
		} else {
		    LOG.debug("No card is present.");
		}

		// calculate what has actually happened
		// removed cards
		Collection<String> cardRemoved = subtract(cardPresent, newCardPresent);
		Collection<StateChangeEvent> crEvents = createEvents(EventType.CARD_REMOVED, cardRemoved);
		// added cards
		Collection<String> cardAdded = subtract(newCardPresent, cardPresent);
		Collection<StateChangeEvent> caEvents = createEvents(EventType.CARD_INSERTED, cardAdded);

		// update internal status with the calculated state
		cardPresent = newCardPresent;
		pendingEvents.addAll(crEvents);
		pendingEvents.addAll(caEvents);
		// use remove so we get an exception when no event has been recorded
		// this would mean our algorithm is corrupt
		LOG.trace("Leaving wait for change with fresh event.");
		try {
		    StateChangeEvent event = pendingEvents.remove();
		    LOG.info("StateChangeEvent: " + event.getState() + " " + event.getTerminal());
		    return event;
		} catch (NoSuchElementException e) {
		    return new StateChangeEvent();
		}
	    }
        }

	private static <T> Collection<T> subtract(Collection<T> a, Collection<T> b) {
	    HashSet<T> result = new HashSet<>(a);
	    result.removeAll(b);
	    return result;
	}

	private static Collection<StateChangeEvent> createEvents(EventType type, Collection<String> list) {
	    Collection<StateChangeEvent> result = new ArrayList<>(list.size());
	    for (String next : list) {
		result.add(new StateChangeEvent(type, next));
	    }
	    return result;
	}

	private void sleep(long millis) throws SCIOException {
	    try {
		Thread.sleep(millis);
	    } catch (InterruptedException ignore) {
	    }
	}
    }
}
