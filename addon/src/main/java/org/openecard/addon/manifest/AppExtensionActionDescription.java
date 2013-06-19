/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon.manifest;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@XmlRootElement(name = "AppExtensionActionDescription")
@XmlType(propOrder = { "id", "className", "localizedName", "localizedDescription", "configDescription" })
public class AppExtensionActionDescription {

    private String id;
    private String className;
    private final List<LocalizedString> localizedName = new ArrayList<LocalizedString>();
    private final List<LocalizedString> localizedDescription = new ArrayList<LocalizedString>();
    private Configuration configDescription;

    @XmlElement(name = "ID")
    public String getId() {
	return id;
    }

    @XmlElement(name = "ClassName")
    public String getClassName() {
	return className;
    }

    @XmlElement(name = "LocalizedName")
    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    @XmlElement(name = "LocalizedDescription")
    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    @XmlElement(name = "ConfigDescription")
    public Configuration getConfigDescription() {
	return configDescription;
    }

    public void setId(String id) {
	this.id = id;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public void setConfigDescription(Configuration configDescription) {
	this.configDescription = configDescription;
    }

}
