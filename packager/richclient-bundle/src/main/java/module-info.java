/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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


module org.openecard.richclient {

	/* 
	add module if you want to use a debugger on the client like this in the ./bin/openecard file
	JLINK_VM_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=5000,server=n,suspend=y"
	*/
    //requires jdk.jdwp.agent;

    requires java.smartcardio;
    requires java.logging;
    requires java.desktop;
    requires java.sql; // for jackson serialization
    requires java.naming;

    /* JAXB module */
    requires java.xml.bind;

    /* JavaFX modules */
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;

    /* EC ciphers for JSSE */
    requires jdk.crypto.ec;

    /* Open JAXB classes for reflection */
    opens de.bund.bsi.ecard.api._1;
    opens iso.std.iso_iec._24727.tech.schema;
    opens oasis.names.tc.dss._1_0.core.schema;
    opens oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_;
    opens oasis.names.tc.saml._1_0.assertion;
    opens oasis.names.tc.saml._2_0.assertion;
    opens org.etsi.uri._01903.v1_3;
    opens org.etsi.uri._02231.v3_1;
    opens org.openecard.ws;
    opens org.openecard.ws.chipgateway;
    opens org.openecard.ws.schema;
    opens org.openecard.addon.bind;
    opens org.openecard.common;
    opens org.openecard.addon;
    opens org.openecard.common.sal.state;
    opens org.openecard.common.event;
    opens org.openecard.common.interfaces;
    opens org.openecard.crypto.common.sal.did;
    opens org.openecard.common.util;
    opens org.openecard.bouncycastle.util.encoders;
    opens org.w3._2000._09.xmldsig_;
    opens org.w3._2001._04.xmldsig_more_;
    opens org.w3._2001._04.xmlenc_;
    opens org.w3._2007._05.xmldsig_more_;
    opens org.w3._2009.xmlenc11_;
    opens generated;

    opens org.openecard.mdlw.sal.config to java.xml.bind;
    opens org.openecard.addon.manifest;

    opens org.openecard.richclient.gui.update;

    opens jnasmartcardio to java.base;

    opens org.jose4j.json.internal.json_simple;
    opens org.slf4j;
	/* JNA needs access to the jnidispatch lib on osx*/
	opens com.sun.jna.darwin;
}
