/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.richclient.activation;


/**
 * Implements an exception for activation errors.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationException extends RuntimeException {

    /**
     * Create a new ActivationException.
     */
    public ActivationException() {
	super();
    }

    /**
     * Create a new ActivationException.
     *
     * @param message Message
     */
    public ActivationException(String message) {
	super(message);
    }

    /**
     * Create a new ActivationException.
     *
     * @param message Message
     * @param throwable Throwable
     */
    public ActivationException(String message, Throwable throwable) {
	super(message, throwable);
    }

}
