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

package org.openecard.client.sal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.sal.ProtocolFactory;


/**
 * Simple class to memorise different protocol factories.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ProtocolFactories {

    private Map<String,ProtocolFactory> factories = new TreeMap<String, ProtocolFactory>();

    /**
     * Checks if the map contains the given protocol URI.
     *
     * @param protocolURI Protocol URI
     * @return 
     */
    public boolean contains(String protocolURI) {
	return factories.containsKey(protocolURI);
    }
    /**
     * Returns a list of protocol URIs.
     *
     * @return List of protocol URIs
     */
    public List<String> protocols() {
	return new ArrayList<String>(factories.keySet());
    }

    /**
     * Returns the protocol factory for a given protocol URI.
     *
     * @param protocolURI Protocol URI
     * @return Protocol factory
     */
    public ProtocolFactory get(String protocolURI) {
	return factories.get(protocolURI);
    }

    /**
     * Adds a new protocol to the factory.
     *
     * @param protocolURI Protocol URI
     * @param protocolFactory Protocol factory
     * @return True if the protocol was added, otherwise false
     */
    public boolean add(String protocolURI, ProtocolFactory protocolFactory) {
	boolean result = false;
	if (!contains(protocolURI)) {
	    result = true;
	    factories.put(protocolURI, protocolFactory);
	}
	return result;
    }

}
