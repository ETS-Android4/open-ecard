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

package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.util.ArrayList;
import org.openecard.client.common.anytype.AuthDataMap;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.w3c.dom.Element;


/**
 * Implements the EAC1InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EAC1InputType {

    public static final String CERTIFICATE = "Certificate";
    public static final String CERTIFICATE_DESCRIPTION = "CertificateDescription";
    public static final String REQUIRED_CHAT = "RequiredCHAT";
    public static final String OPTIONAL_CHAT = "OptionalCHAT";
    public static final String AUTHENTICATED_AUXILIARY_DATA = "AuthenticatedAuxiliaryData";

    private final AuthDataMap authMap;
    private ArrayList<CardVerifiableCertificate> certificates;
    private byte[] certificateDescription;
    private byte[] requiredCHAT;
    private byte[] optionalCHAT;
    private byte[] authenticatedAuxiliaryData;

    /**
     * Creates a new EAC1InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws Exception
     */
    public EAC1InputType(DIDAuthenticationDataType baseType) throws Exception {
	authMap = new AuthDataMap(baseType);

	certificateDescription = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION);
	requiredCHAT = authMap.getContentAsBytes(REQUIRED_CHAT);
	optionalCHAT = authMap.getContentAsBytes(OPTIONAL_CHAT);
	authenticatedAuxiliaryData = authMap.getContentAsBytes(AUTHENTICATED_AUXILIARY_DATA);

	certificates = new ArrayList<CardVerifiableCertificate>();
	for (Element element : baseType.getAny()) {
	    if (element.getLocalName().equals(CERTIFICATE)) {
		byte[] value = StringUtils.toByteArray(element.getTextContent());
		CardVerifiableCertificate cvc = new CardVerifiableCertificate(value);
		certificates.add(cvc);
	    }
	}
    }

    /**
     * Returns the set of certificates.
     *
     * @return Certificates
     */
    public ArrayList<CardVerifiableCertificate> getCertificates() {
	return certificates;
    }

    /**
     * Returns the certificate description.
     *
     * @return Certificate description
     */
    public byte[] getCertificateDescription() {
	return certificateDescription;
    }

    /**
     * Returns the required CHAT.
     *
     * @return Required CHAT
     */
    public byte[] getRequiredCHAT() {
	return requiredCHAT;
    }

    /**
     * Returns the optional CHAT.
     *
     * @return Optional CHAT
     */
    public byte[] getOptionalCHAT() {
	return optionalCHAT;
    }

    /**
     * Returns the AuthenticatedAuxiliaryData.
     *
     * @return AuthenticatedAuxiliaryData
     */
    public byte[] getAuthenticatedAuxiliaryData() {
	return authenticatedAuxiliaryData;
    }

    /**
     * Returns a new EAC1OutputType.
     *
     * @return EAC1OutputType
     */
    public EAC1OutputType getOutputType() {
	return new EAC1OutputType(authMap);
    }

}
