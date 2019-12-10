/** **************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
 ************************************************************************** */
package org.openecard.scio;

import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
public final class AndroidNFCCard extends AbstractNFCCard {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNFCCard.class);

    private final int transceiveTimeout;
    private final byte[] histBytes;
    private final IsoDep isodep;
    private NFCCardMonitoring cardMonitor;
    private final Object connectLock = new Object();

    private Thread monitor;

    public AndroidNFCCard(IsoDep tag, int timeout, NFCCardTerminal terminal) throws IOException {
	super(terminal);
	isodep = tag;
	transceiveTimeout = timeout;

	byte[] histBytesTmp = isodep.getHistoricalBytes();
	if (histBytesTmp == null) {
	    histBytesTmp = isodep.getHiLayerResponse();
	}
	this.histBytes = histBytesTmp;

	connectTag();

	// start thread which monitors the availability of the card
	startCardMonitor();
    }

    private void connectTag() throws IOException {
	isodep.connect();
	isodep.setTimeout(getTransceiveTimeout());
    }

    private void startCardMonitor() {
	NFCCardMonitoring createdMonitor = new NFCCardMonitoring(nfcCardTerminal, this);
	Thread executionThread = new Thread();
	executionThread.start();
	this.monitor = executionThread;
	this.cardMonitor = createdMonitor;
    }

    private int getTransceiveTimeout() {
	return transceiveTimeout;
    }

    @Override
    public void disconnect(boolean reset) throws SCIOException {
	if (reset) {
	    synchronized(connectLock) {
		terminateTag();

		try {
		    connectTag();

		    // start thread which is monitoring the availability of the card
		    startCardMonitor();
		} catch (IOException ex) {
		    LOG.error("Failed to connect NFC tag.", ex);
		    throw new SCIOException("Failed to reset channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex);
		}
	    }
	}
    }

    @Override
    public boolean isTagPresent() {
	return isodep.isConnected();
    }

    @Override
    public void terminateTag() throws SCIOException {
	try {
	    this.terminateTag(this.monitor, cardMonitor);
	} catch (IOException ex) {
	    LOG.error("Failed to close NFC tag.");
	    throw new SCIOException("Failed to close NFC channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex);
	}
    }

    private void terminateTag(Thread monitor, NFCCardMonitoring cardMonitor) throws IOException {
	synchronized(connectLock) {
	    if (monitor != null) {
		LOG.debug("Killing the monitor");
		cardMonitor.notifyStopMonitoring();
	    }

	    this.isodep.close();
	}
    }

    @Override
    public SCIOATR getATR() {
	// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
	if (histBytes == null) {
	    return new SCIOATR(new byte[0]);
	} else {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    // Initial Header
	    out.write(0x3B);
	    // T0
	    out.write(0x80 | (histBytes.length & 0xF));
	    // TD1
	    out.write(0x80);
	    // TD2
	    out.write(0x01);
	    // ISO14443A: The historical bytes from ATS response.
	    // ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
	    // TODO: check that the HiLayerResponse matches the requirements for ISO14443B
	    out.write(histBytes, 0, histBytes.length);

	    // TCK: Exclusive-OR of bytes T0 to Tk
	    byte[] preATR = out.toByteArray();
	    byte chkSum = 0;
	    for (int i = 1; i < preATR.length; i++) {
		chkSum ^= preATR[i];
	    }
	    out.write(chkSum);

	    byte[] atr = out.toByteArray();
	    return new SCIOATR(atr);
	}
    }

    @Override
    public byte[] transceive(byte[] apdu) throws IOException {
	try {
	    return isodep.transceive(apdu);
	} catch (TagLostException ex) {
	    LOG.debug("NFC Tag is not present.", ex);
	    this.nfcCardTerminal.removeTag();
	    throw new IllegalStateException("Transmit of apdu command failed, because the card is not present.");
	}
    }

}
