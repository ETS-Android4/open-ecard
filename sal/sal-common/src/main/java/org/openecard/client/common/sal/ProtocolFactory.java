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

package org.openecard.client.common.sal;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.gui.UserConsent;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface ProtocolFactory {

    /**
     * Get URI of the protocol the instances created by this factory supports.
     *
     * @return URI of the supported protocol
     */
    public String getProtocol();

    /**
     * Create instance of the protocol.
     *
     * @return instance of the protocol which can be used for one connection
     */
    public Protocol createInstance(Dispatcher dispatcher, UserConsent gui);

}
