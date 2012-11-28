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

package org.openecard.crypto.tls;

import java.io.IOException;
import org.openecard.bouncycastle.crypto.tls.Certificate;

/**
 * This class extends {@link org.openecard.crypto.tls.TlsNoAuthentication TlsNoAuthentication} and adds an additional
 * getter for obtaining the processed certificate chain.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class TlsNoAuthentication2 extends TlsNoAuthentication {

    private Certificate lastCertChain;

    @Override
    public void notifyServerCertificate(Certificate crtfct) throws IOException {
	lastCertChain = crtfct;
	super.notifyServerCertificate(crtfct);
    }

    /**
     * Returns the certificate chain which is processed during the TLS authentication.
     *
     * @return certificate chain
     */
    public Certificate getCertificateChain() {
	return lastCertChain;
    }

}
