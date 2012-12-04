/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.gui.android;

import android.content.Context;
import org.openecard.gui.FileDialog;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidUserConsent implements org.openecard.gui.UserConsent {

    private Context context;

    public AndroidUserConsent(Context context) {
	this.context = context;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription arg0) {
	return new AndroidNavigator(arg0.getSteps(), this.context);
    }

    @Override
    public FileDialog obtainFileDialog() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
