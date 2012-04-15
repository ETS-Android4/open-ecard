/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.apdu.common;

import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.ws.IFD;


/**
 * Implements a command APDU.
 * See ISO/IEC 7816-4 Section 5.1.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardCommandAPDU extends CardAPDU {

    private static final Logger logger = Logger.getLogger("APDU");
    private byte[] header = new byte[4];
    private int le = -1;
    private int lc = -1;

    /**
     * Creates a new command APDU.
     */
    protected CardCommandAPDU() {
    }

    /**
     * Creates a new command APDU.
     *
     * @param commandAPDU APDU
     */
    public CardCommandAPDU(byte[] commandAPDU) {
	System.arraycopy(commandAPDU, 0, header, 0, 4);
	System.arraycopy(commandAPDU, 4, data, 0, commandAPDU.length - 4);
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2) {
	header[0] = cla;
	header[1] = ins;
	header[2] = p1;
	header[3] = p2;
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte le) {
	this(cla, ins, p1, p2, (int) (le & 0xFF));
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, short le) {
	this(cla, ins, p1, p2, (int) (le & 0xFFFF));
    }

    /**
     * Create a new command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, int le) {
	this(cla, ins, p1, p2);
	this.le = le;
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data) {
	this(cla, ins, p1, p2);
	this.data = data;

	setLC(data.length);
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data, byte le) {
	this(cla, ins, p1, p2, data, (int) (le & 0xFF));
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data, short le) {
	this(cla, ins, p1, p2, data, (int) (le & 0xFFFF));
    }

    /**
     * Create a new Command APDU.
     *
     * @param cla Class byte
     * @param ins Instruction byte
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data field
     * @param le Length expected field
     */
    public CardCommandAPDU(byte cla, byte ins, byte p1, byte p2, byte[] data, int le) {
	this(cla, ins, p1, p2, data);

	setLE(le);
    }

    /**
     * Sets the class byte of the APDU.
     *
     * @param cla Class byte
     */
    protected void setCLA(byte cla) {
	header[0] = cla;
    }

    /**
     * Returns the class byte of the APDU.
     *
     * @return Class byte
     */
    public byte getCLA() {
	return header[0];
    }

    /**
     * Sets the instruction byte of the APDU.
     *
     * @param ins Instruction byte
     */
    protected void setINS(byte ins) {
	header[1] = ins;
    }

    /**
     * Returns the instruction byte of the APDU.
     *
     * @return Instruction byte
     */
    public byte getINS() {
	return header[1];
    }

    /**
     * Sets the parameter byte P1 of the APDU.
     *
     * @param p1 Parameter byte P1
     */
    protected void setP1(byte p1) {
	header[2] = p1;
    }

    /**
     * Returns the parameter byte P1 of the APDU.
     *
     * @return Parameter byte P1
     */
    public byte getP1() {
	return header[2];
    }

    /**
     * Sets the parameter byte P2 of the APDU.
     *
     * @param p2 Parameter byte P2
     */
    protected void setP2(byte p2) {
	header[3] = p2;
    }

    /**
     * Returns the parameter byte P2 of the APDU.
     *
     * @return parameter byte P2
     */
    public byte getP2() {
	return header[3];
    }

    /**
     * Sets the parameter bytes P1 and P2 of the APDU.
     *
     * @param p1p2 Parameter bytes P1 and P2
     */
    protected void setP1P2(byte[] p1p2) {
	setP1(p1p2[0]);
	setP2(p1p2[1]);
    }

    /**
     * Returns the parameter bytes P1 and P2 of the APDU.
     *
     * @return parameter bytes P1 and P2
     */
    public byte[] getP1P2() {
	return new byte[]{getP1(), getP2()};
    }

    /**
     * Returns the header of the APDU: CLA | INS | P1 | P2
     *
     * @return Header of the APDU
     */
    public byte[] getHeader() {
	return header;
    }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected final void setLC(byte lc) {
	setLC((int) (lc & 0xFF));
    }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected final void setLC(short lc) {
	setLC((int) (lc & 0xFFFF));
    }

    /**
     * Sets the length command (LC) field of the APDU.
     *
     * @param lc Length command (LC) field
     */
    protected final void setLC(int lc) {
	if (lc < 1 || lc > 65536) {
	    throw new IllegalArgumentException("Length should be from '1' to '65535'.");
	}
	this.lc = lc;
    }

    /**
     * Returns the length command (LC) field.
     *
     * @return Length command (LC) field
     */
    public int getLC() {
	return lc;
    }

    /**
     * Sets the data field of the APDU. Length command (LC) field will be calculated.
     *
     * @param data Data field
     */
    @Override
    public void setData(byte[] data) {
	super.setData(data);
	setLC(data.length);
    }

    /**
     * Sets the body (LE, DATA, LC) of the APDU.
     *
     * @param body Body of the APDU
     */
    public void setBody(byte[] body) {

	/*
	 * Case 1. : |CLA|INS|P1|P2|
	 * Case 2. : |CLA|INS|P1|P2|LE|
	 * Case 2.1: |CLA|INS|P1|P2|EXTLE|
	 * Case 3. : |CLA|INS|P1|P2|LC|DATA|
	 * Case 3.1: |CLA|INS|P1|P2|EXTLC|DATA|
	 * Case 4. : |CLA|INS|P1|P2|LC|DATA|LE|
	 * Case 4.1: |CLA|INS|P1|P2|EXTLC|DATA|LE|
	 * Case 4.2: |CLA|INS|P1|P2|LC|DATA|EXTLE|
	 * Case 4.3: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
	 */
	try {
	    ByteArrayInputStream bais = new ByteArrayInputStream(body);
	    int length = bais.available();

	    // Cleanup
	    lc = -1;
	    le = -1;
	    data = new byte[0];

	    if (length == 1) {
		// Case 2 |CLA|INS|P1|P2|LE|
		le = (bais.read() & 0xFF);
	    } else if (length < 65536) {
		int tmp = bais.read();

		if (tmp == 0) {
		    // Case 2.1, 3.1, 4.1, 4.3
		    if (bais.available() < 3) {
			// Case 2.1 |CLA|INS|P1|P2|EXTLE|
			le = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);
		    } else {
			// Case 3.1, 4.1, 4.3
			lc = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);

			data = new byte[lc];
			bais.read(data);

			if (bais.available() == 1) {
			    // Case 4.1 |CLA|INS|P1|P2|EXTLC|DATA|LE|
			    le = (bais.read() & 0xFF);
			} else if (bais.available() == 2) {
			    // Case 4.3 |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
			    le = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);
			} else if (bais.available() == 3) {
			    if (bais.read() == 0) {
				// Case 4.3 |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
				le = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);
			    } else {
				throw new IllegalArgumentException("Malformed APDU.");
			    }
			} else if (bais.available() > 3) {
			    throw new IllegalArgumentException("Malformed APDU.");
			}
		    }
		} else if (tmp > 0) {
		    // Case 3, 4, 4.2
		    lc = (tmp & 0xFF);
		    data = new byte[lc];
		    bais.read(data);

		    if (bais.available() == 1 || bais.available() == 3) {
			tmp = bais.read();
			if (tmp != 0) {
			    // Case 4 |CLA|INS|P1|P2|LC|DATA|LE|
			    le = (tmp & 0xFF);
			} else {
			    // Case 4.2 |CLA|INS|P1|P2|LC|DATA|EXTLE|
			    le = ((bais.read() & 0xFF) << 8) | (bais.read() & 0xFF);
			}
		    } else if (bais.available() == 2 || bais.available() > 3) {
			throw new IllegalArgumentException("Malformed APDU.");
		    }
		} else {
		    throw new IllegalArgumentException("Malformed APDU.");
		}
	    } else {
		throw new IllegalArgumentException("Malformed APDU.");
	    }
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "Exception", e);
	}
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    public final void setLE(byte le) {
	if (le == (byte) 0x00) {
	    setLE(256);
	} else {
	    setLE((int) (le & 0xFF));
	}
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    public final void setLE(short le) {
	if (le == (short) 0x0000) {
	    setLE(65536);
	} else {
	    setLE((int) (le & 0xFFFF));
	}
    }

    /**
     * Sets the length expected (LE) field of the APDU.
     *
     * @param le Length expected (LE) field
     */
    public final void setLE(int le) {
	if (le < 0 || le > 65536) {
	    throw new IllegalArgumentException("Length should be from '1' to '65535'.");
	} else {
	    this.le = le;
	}
    }

    /**
     * Returns the length expected (LE) field
     *
     * @return Length expected (LE) field
     */
    public final int getLE() {
	return le;
    }

    /**
     * Updates the class byte of the header to indicate command chaining.
     * See ISO/IEC 7816-4 Section 5.1.1.1
     */
    public final void setChaining() {
	header[0] = (byte) (header[0] | 0x10);
    }

    public final void getChainingIterator() {
	//TODO
	throw new IllegalAccessError("Not implemented yet");
    }

    /**
     * Updates the class byte of the header to indicate Secure Messaging.
     * See ISO/IEC 7816-4 Section 6
     */
    public final void setSecureMessaging() {
	header[0] = (byte) (header[0] | 0x0C);
    }

    /**
     * Return true if the header of the APDU indicates Secure Messaging.
     *
     * @return True if APDU is a Secure Messaging APDU.
     */
    public final boolean isSecureMessaging() {
	return ((header[0] & (byte) 0x0F) == (byte) 0x0C);
    }

    /**
     * Returns the encoded APDU: CLA | INS | P1 | P2 | (EXT)LC | DATA | (EXT)LE
     *
     * @return Encoded APDU
     */
    public final byte[] toByteArray() {
	ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length + 10);

	try {
	    // Write APDU header
	    baos.write(header);
	    // Write APDU LC field.
	    if (lc > 255) {
		// Encoded extended LC field in three bytes.
		baos.write(x00);
		baos.write((byte) (lc >> 8));
		baos.write((byte) lc);
	    } else if (lc > 0) {
		// Write short LC field
		baos.write((byte) lc);
	    }
	    // Write APDU data field
	    baos.write(data);
	    // Write APDU LE field.
	    if (le > 256) {
		// Write extended LE field.
		if (lc < 256) {
		    // Encoded extended LC field in three bytes.
		    baos.write(x00);
		}
		// Encoded extended LC field in two bytes if extended LC field is present.
		if (le == 65536) {
		    baos.write(x00);
		    baos.write(x00);
		}
		baos.write((byte) (le >> 8));
		baos.write((byte) le);
	    } else if (le == 256) {
		// Write short LE field
		baos.write(x00);
	    } else if (le > 0) {
		// Write short LE field
		baos.write((byte) le);
	    }
	} catch (Exception e) {
	    logger.log(Level.SEVERE, "Exception", e);
	}

	return baos.toByteArray();
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    public String toHexString() {
	return ByteUtils.toHexString(toByteArray());
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    @Override
    public String toString() {
	return ByteUtils.toHexString(toByteArray(), true);
    }

    /**
     * Creates a new Transmit message.
     *
     * @param slotHandle Slot handle
     * @return Transmit
     */
    public Transmit makeTransmit(byte[] slotHandle) {
	ArrayList<byte[]> positiveResponses = new ArrayList<byte[]>() {

	    {
		add(new byte[]{(byte) 0x90, (byte) 0x00});
	    }
	};
	return makeTransmit(slotHandle, positiveResponses);
    }

    /**
     * Creates a new Transmit message.
     *
     * @param slotHandle Slot handle
     * @param responses Positive responses
     * @return Transmit
     */
    public Transmit makeTransmit(byte[] slotHandle, List<byte[]> responses) {
	Transmit t = new Transmit();
	InputAPDUInfoType apdu = new InputAPDUInfoType();

	apdu.setInputAPDU(toByteArray());
	apdu.getAcceptableStatusCode().addAll(responses);

	t.setSlotHandle(slotHandle);
	t.getInputAPDUInfo().add(apdu);

	return t;
    }

    /**
     * Transmit the APDU.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     * @return Response APDU
     */
    public CardResponseAPDU transmit(IFD ifd, byte[] slotHandle) throws WSException {
	Transmit t = makeTransmit(slotHandle);
	TransmitResponse tr = ifd.transmit(t);
	WSHelper.checkResult(tr);
	CardResponseAPDU responseAPDU = new CardResponseAPDU(tr);

	return responseAPDU;
    }

    /**
     * Transmit the APDU.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     * @param responses List of positive responses
     * @return Response APDU
     */
    public CardResponseAPDU transmit(IFD ifd, byte[] slotHandle, List<byte[]> responses) throws WSException {
	Transmit t = makeTransmit(slotHandle, responses);
	TransmitResponse tr = ifd.transmit(t);
	WSHelper.checkResult(tr);
	CardResponseAPDU responseAPDU = new CardResponseAPDU(tr);

	return responseAPDU;
    }

    /**
     * Transmit the APDU.
     *
     * @param ifd IFD
     * @param slotHandle Slot handle
     * @param responses List of positive responses
     * @return Response APDU
     */
    public CardResponseAPDU transmit(IFD ifd, byte[] slotHandle, byte[]... responses) throws WSException {
	Transmit t = new Transmit();
	InputAPDUInfoType apdu = new InputAPDUInfoType();
	apdu.setInputAPDU(toByteArray());

	for (int i = 1; i < responses.length; i++) {
	    apdu.getAcceptableStatusCode().add(responses[i]);
	}

	t.setSlotHandle(slotHandle);
	t.getInputAPDUInfo().add(apdu);

	TransmitResponse tr = ifd.transmit(t);
	WSHelper.checkResult(tr);
	CardResponseAPDU responseAPDU = new CardResponseAPDU(tr);

	return responseAPDU;
    }

}
