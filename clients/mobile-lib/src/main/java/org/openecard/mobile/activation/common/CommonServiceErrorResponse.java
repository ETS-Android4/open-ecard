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

import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.system.ServiceErrorCode;

/**
 *
 * @author Neil Crossley
 */
public class CommonServiceErrorResponse implements ServiceErrorResponse {

    private final ServiceErrorCode statusCode;
    private final String message;

    public CommonServiceErrorResponse(ServiceErrorCode statusCode, String message) {
	this.statusCode = statusCode;
	this.message = message;
    }

    public ServiceErrorCode getStatusCode() {
	return statusCode;
    }

    public String getMessage() {
	return message;
    }
}
