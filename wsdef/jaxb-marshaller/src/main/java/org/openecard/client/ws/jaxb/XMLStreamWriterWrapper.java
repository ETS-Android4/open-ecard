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

package org.openecard.client.ws.jaxb;

import java.util.LinkedList;
import java.util.TreeSet;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * Wraps {@link XMLStreamWriter} to get namespace prefix customization working.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class XMLStreamWriterWrapper implements XMLStreamWriter {

    private static class PrefixList {
	public final String localName;
	public final TreeSet<String> prefixes = new TreeSet<String>();

	public PrefixList(String localName) {
	    this.localName = localName;
	}
    }

    private final XMLStreamWriter writer;
    private LinkedList<PrefixList> hierarchy = new LinkedList<PrefixList>();
    private TreeSet<String> activePrefixes = new TreeSet<String>();


    public XMLStreamWriterWrapper(XMLStreamWriter writer) throws XMLStreamException {
	writer.setPrefix("iso", "urn:iso:std:iso-iec:24727:tech:schema");
	this.writer = writer;
    }


    private void pop() {
	PrefixList list = hierarchy.pop();
	activePrefixes.removeAll(list.prefixes);
    }

    private void push(String localName) {
	hierarchy.push(new PrefixList(localName));
    }

    private void addNS(String prefix, String ns) throws XMLStreamException {
	if (! activePrefixes.contains(prefix)) {
	    activePrefixes.add(prefix);
	    hierarchy.peek().prefixes.add(prefix);
	    writer.writeNamespace(prefix, ns);
	}
    }


    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	push(localName);
	writer.writeStartElement(prefix, localName, namespaceURI);
	writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
	pop();
	writer.writeEndElement();
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
	addNS(prefix, namespaceURI);
    }


    ///
    /// all following methods remain unchanged
    ///

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
	writer.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
	writer.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
	writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
	writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
	writer.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
	writer.close();
    }

    @Override
    public void flush() throws XMLStreamException {
	writer.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
	writer.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
	writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
	writer.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
	writer.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
	writer.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
	writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
	writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
	writer.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
	writer.writeDTD(dtd);

    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
	writer.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
	writer.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
	writer.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
	writer.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
	writer.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
	writer.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
	return writer.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
	writer.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
	writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
	writer.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
	return writer.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
	return writer.getProperty(name);
    }

}
