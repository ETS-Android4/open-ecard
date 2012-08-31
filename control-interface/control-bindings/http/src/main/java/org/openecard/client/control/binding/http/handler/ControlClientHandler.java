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

package org.openecard.client.control.binding.http.handler;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.binding.http.HTTPException;
import org.openecard.client.control.binding.http.common.Http11Response;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListener;
import org.openecard.client.control.client.ControlListeners;
import org.openecard.client.control.handler.ControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class ControlClientHandler extends ControlHandler implements HttpRequestHandler {

    private static final Logger _logger = LoggerFactory.getLogger(ControlClientHandler.class);
    private ControlListeners listeners;

    /**
     * Creates a new ControlRequestHandler.
     *
     * @param listeners ControlListeners
     * @param path Path
     */
    public ControlClientHandler(String path, ControlListeners listeners) {
	super(path);
	this.listeners = listeners;
    }

    /**
     * Handles a HTTP request and creates a client request.
     *
     * @param httpRequest HTTP request
     * @return A client request or null
     * @throws ControlException 
     * @throws Exception If the request should be handled by the handler but is malformed
     */
    public abstract ClientRequest handleRequest(HttpRequest httpRequest) throws ControlException, Exception;

    /**
     * Handles a client response and creates a HTTP response.
     *
     * @param clientResponse Client response
     * @return A HTTP response
     * @throws ControlException 
     * @throws Exception
     */
    public abstract HttpResponse handleResponse(ClientResponse clientResponse) throws ControlException, Exception;

    /**
     * Handles a HTTP request.
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @param context HttpContext
     * @throws HttpException
     * @throws IOException
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
	_logger.debug("HTTP request: {}", request.toString());
	HttpResponse httpResponse = null;

	try {
	    ClientRequest clientRequest = handleRequest(request);
	    ClientResponse clientResponse = null;

	    if (clientRequest == null) {
		throw new ControlException();
	    }

	    if (listeners.getControlListeners().isEmpty()) {
		throw new HTTPException(HttpStatus.SC_NOT_FOUND);
	    }

	    for (ControlListener listener : listeners.getControlListeners()) {
		clientResponse = listener.request(clientRequest);
		if (clientResponse != null) {
		    break;
		}
	    }

	    // Forward request parameters to response parameters
	    response.setParams(request.getParams());

	    httpResponse = handleResponse(clientResponse);
	} catch (ControlException e) {
	    httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);

	    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
		httpResponse.setEntity(new StringEntity(e.getMessage(), "UTF-8"));
	    }

	    if (e instanceof HTTPException) {
		httpResponse.setStatusCode(((HTTPException) e).getHTTPStatusCode());
	    }
	} catch (Exception e) {
	    httpResponse = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    _logger.error("Exception", e);
	} finally {
	    Http11Response.copyHttpResponse(httpResponse, response);
	    _logger.debug("HTTP response: {}", response);
	    _logger.debug("HTTP request handled by: {}", this.getClass().getName());
	}
    }

}
