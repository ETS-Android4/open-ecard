/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;

/**
 *
 * @author Neil Crossley
 */
public class CommonActivationResult implements ActivationResult {

    private final String redirectUrl;
    private final ActivationResultCode resultCode;
    private final String errorMessage;

    public CommonActivationResult(ActivationResultCode resultCode) {
	this(null, resultCode, null);
    }

    public CommonActivationResult(ActivationResultCode resultCode, String errorMessage) {
	this(null, resultCode, errorMessage);
    }

    public CommonActivationResult(String redirectUrl, ActivationResultCode resultCode) {
	this(redirectUrl, resultCode, null);
    }

    public CommonActivationResult(String redirectUrl, ActivationResultCode resultCode, String errorMessage) {
	this.redirectUrl = redirectUrl;
	this.resultCode = resultCode;
	this.errorMessage = errorMessage;
    }

    public String getRedirectUrl() {
	return redirectUrl;
    }

    public ActivationResultCode getResultCode() {
	return resultCode;
    }

    public String getErrorMessage() {
	return errorMessage;
    }

}