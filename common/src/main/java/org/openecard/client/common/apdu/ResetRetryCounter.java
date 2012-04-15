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
package org.openecard.client.common.apdu;

import org.openecard.client.common.apdu.common.CardCommandAPDU;


/**
 * RESET RETRY COUNTER command
 * See ISO/IEC 7816-4 Section 7.5.10
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ResetRetryCounter extends CardCommandAPDU {

    /**
     * RESET RETRY COUNTER command instruction byte
     */
    private static final byte RESET_RETRY_COUNTER_INS = (byte) 0x2C;

    /**
     * Creates a RESET RETRY COUNTER APDU.
     *
     * @param password Password
     * @param type Password type
     */
    public ResetRetryCounter(byte[] password, byte type) {
	super(x00, RESET_RETRY_COUNTER_INS, (byte) 0x02, type);
	setData(password);
    }

    /**
     * Creates a RESET RETRY COUNTER APDU.
     *
     * @param type Password type
     */
    public ResetRetryCounter(byte type) {
	super(x00, RESET_RETRY_COUNTER_INS, (byte) 0x03, type);
    }

}
