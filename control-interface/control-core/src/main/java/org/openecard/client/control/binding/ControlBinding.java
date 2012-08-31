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

package org.openecard.client.control.binding;

import org.openecard.client.control.client.ControlListeners;
import org.openecard.client.control.handler.ControlHandlers;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class ControlBinding {

    protected ControlHandlers handlers;
    protected ControlListeners listeners;

    /**
     * Sets the control handlers.
     * 
     * @param handlers Control handlers
     */
    public void setControlHandlers(ControlHandlers handlers) {
	this.handlers = handlers;
    }

    /**
     * Sets the control listeners.
     * 
     * @param listeners Control listeners
     */
    public void setControlListeners(ControlListeners listeners) {
	this.listeners = listeners;
    }

    /**
     * Starts the binding.
     */
    public abstract void start() throws Exception;

    /**
     * Stops the binding.
     */
    public abstract void stop() throws Exception;

}
