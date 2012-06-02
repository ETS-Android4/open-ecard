package org.openecard.client.connector.handler;

import java.net.URI;
import java.net.URL;
import java.util.List;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.common.ErrorPage;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.openecard.client.connector.http.header.RequestLine;
import org.openecard.client.connector.http.header.ResponseHeader;
import org.openecard.client.connector.http.header.StatusLine;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.messages.TCTokenResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;
import org.openecard.client.connector.tctoken.TCToken;
import org.openecard.client.connector.tctoken.TCTokenConverter;
import org.openecard.client.connector.tctoken.TCTokenException;
import org.openecard.client.connector.tctoken.TCTokenGrabber;
import org.openecard.client.connector.tctoken.TCTokenParser;
import org.openecard.client.connector.tctoken.TCTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandler implements ConnectorHandler {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenHandler.class);

    /**
     * Create a new ActivationRequest.
     */
    public TCTokenHandler() {
    }

    @Override
    public ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception {
	RequestLine requestLine = httpRequest.getRequestLine();

	if (requestLine.getMethod().equals(RequestLine.Methode.GET.name())) {
	    URI requestURI = requestLine.getRequestURI();

	    if (requestURI.getPath().equals("/eID-Client")) {
		TCTokenRequest tcTokenRequest = new TCTokenRequest();
		String query[] = requestURI.getQuery().split("&");

		for (String q : query) {
		    String name = q.substring(0, q.indexOf("="));
		    String value = q.substring(q.indexOf("=") + 1, q.length());

		    if (name.startsWith("tcTokenURL")) {
			if (!value.isEmpty()) {
			    TCToken token = parseTCToken(new URL(value));
			    tcTokenRequest.setTCToken(token);
			} else {
			    throw new IllegalArgumentException("Malformed tcTokenURL");
			}

		    } else if (name.startsWith("slotHandle")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setSlotHandle(value);
			} else {
			    throw new IllegalArgumentException("Malformed slot handle");
			}
		    } else if (name.startsWith("contextHandle")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setContextHandle(value);
			} else {
			    throw new IllegalArgumentException("Malformed context handle");
			}
		    } else {
			// <editor-fold defaultstate="collapsed" desc="log unknown query element">
			logger.info(LoggingConstants.FINE, "Unknown query element: {}", name);
			// </editor-fold>
		    }
		}

		return tcTokenRequest;
	    }
	}

	return null;
    }

    @Override
    public HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception {
	if (clientResponse instanceof TCTokenResponse) {
	    TCTokenResponse response = (TCTokenResponse) clientResponse;

	    if (response.getErrorPage() != null) {
		return handleErrorPage(response.getErrorPage());
	    } else if (response.getErrorMessage() != null) {
		return handleErrorResponse(response.getErrorMessage());
	    } else if (response.getRefreshAddress() != null) {
		return handleRedirectResponse(response.getRefreshAddress());
	    } else {
		return handleErrorResponse(ConnectorConstants.ConnectorError.INTERNAL_ERROR.toString());
	    }
	}
	return null;
    }

    /**
     * Parses the TCToken.
     *
     * @throws TCTokenException
     */
    private TCToken parseTCToken(URL tokenURI) throws TCTokenException {
	// Get TCToken from the given url
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI.toString());

	//FIXME Remove me
	TCTokenConverter converter = new TCTokenConverter();
	data = converter.convert(data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();

	return tokens.get(0);
    }

    /**
     * Handle a redirect response.
     *
     * @param location Redirect location
     * @return HTTP response
     */
    public HTTPResponse handleRedirectResponse(URL location) {
	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.SEE_OTHER_303));
	httpResponse.addResponseHeaders(new ResponseHeader(ResponseHeader.Field.LOCATION, location.toString()));

	return httpResponse;
    }

    /**
     * Handle a error response.
     * The message will be placed in a HTML page.
     *
     * @param message Message
     * @return HTTP response
     */
    public HTTPResponse handleErrorResponse(String message) {
	ErrorPage p = new ErrorPage(message);
	String content = p.getHTML();

	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(content);

	return httpResponse;
    }

    /**
     * Handle a error HTML page.
     *
     * @param content Content
     * @return HTTP response
     */
    public HTTPResponse handleErrorPage(String content) {

	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(content);

	return httpResponse;
    }
}
