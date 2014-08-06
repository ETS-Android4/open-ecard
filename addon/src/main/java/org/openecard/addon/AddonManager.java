/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

package org.openecard.addon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppExtensionActionProxy;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AppPluginActionProxy;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.ifd.IFDProtocolProxy;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.addon.sal.SALProtocolProxy;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventManager;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.FacadeInvocationHandler;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of a AddonManager.
 *
 * The AddonManager takes care for the management of the add-on. This covers the initialization startup, registering of
 * new add-ons, unloading of add-ons on shut down and removal and the uninstalling of an add-on. Furthermore the
 * AddonManager provides methods to retrieve specific parts of a add-on.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AddonManager {

    private static final Logger logger = LoggerFactory.getLogger(AddonManager.class);

    private final CombiningRegistry registry;
    private final AddonRegistry protectedRegistry;
    private final Dispatcher dispatcher;
    private final UserConsent userConsent;
    private final CardStateMap cardStates;
    private final CardRecognition recognition;
    private final EventManager eventManager;
    private final EventHandler eventHandler;
    private final TreeMap<AddonSpecification, TreeMap<String, IFDProtocol>> ifdProtocolCache = new TreeMap<>();
    private final TreeMap<AddonSpecification, TreeMap<String, SALProtocol>> salProtocolCache = new TreeMap<>();
    private final TreeMap<AddonSpecification, TreeMap<String, AppExtensionAction>> appExtActionCache = new TreeMap<>();
    private final TreeMap<AddonSpecification, TreeMap<String, AppPluginAction>> appPluginActionCache = new TreeMap<>();

    /**
     * Creates a new AddonManager.
     *
     * @param dispatcher
     * @param userConsent
     * @param cardStates
     * @param recognition
     * @param eventManager
     * @throws WSMarshallerException
     */
    public AddonManager(Dispatcher dispatcher, UserConsent userConsent, CardStateMap cardStates,
	    CardRecognition recognition, EventManager eventManager) throws WSMarshallerException {

	this.registry = new CombiningRegistry(this);
	this.protectedRegistry = getProtectedRegistry(registry);
	this.dispatcher = dispatcher;
	this.userConsent = userConsent;
	this.cardStates = cardStates;
	this.recognition = recognition;
	this.eventManager = eventManager;
	this.eventHandler = new EventHandler(eventManager);

	loadLoadOnStartAddons();
    }

    /**
     * Load all addons which contain an loadOnStart = true.
     *
     * @throws WSMarshallerException
     */
    private void loadLoadOnStartAddons() throws WSMarshallerException {
	// load plugins which have an loadOnStartup = true
	Set<AddonSpecification> specs = protectedRegistry.listAddons();
	for (AddonSpecification addonSpec : specs) {
	    loadLoadOnStartupActions(addonSpec);
	}
    }

    /**
     * Load a single addon which contains a LoadOnStartup = true.
     *
     * @param addonSpec The {@link AddonSpecification} of the addon.
     */
    protected void loadLoadOnStartupActions(AddonSpecification addonSpec) {
	if (!addonSpec.getApplicationActions().isEmpty()) {
	    for (AppExtensionSpecification appExSpec : addonSpec.getApplicationActions()) {
		if (appExSpec.isLoadOnStartup()) {
		    getAppExtensionAction(addonSpec, appExSpec.getId());
		}
	    }
	}

	if (!addonSpec.getBindingActions().isEmpty()) {
	    for (AppPluginSpecification appPlugSpec : addonSpec.getBindingActions()) {
		if (appPlugSpec.isLoadOnStartup()) {
		    getAppPluginAction(addonSpec, appPlugSpec.getResourceName());
		}
	    }
	}

	if (!addonSpec.getIfdActions().isEmpty()) {
	    for (ProtocolPluginSpecification protPlugSpec : addonSpec.getIfdActions()) {
		if (protPlugSpec.isLoadOnStartup()) {
		    getIFDProtocol(addonSpec, protPlugSpec.getUri());
		}
	    }
	}

	if (!addonSpec.getSalActions().isEmpty()) {
	    for (ProtocolPluginSpecification protPlugSpec : addonSpec.getSalActions()) {
		if (protPlugSpec.isLoadOnStartup()) {
		    getSALProtocol(addonSpec, protPlugSpec.getUri());
		}
	    }
	}
    }

    /**
     * Unload all add-ons.
     */
    private void unloadAllAddons() {
	Set<AddonSpecification> addons = protectedRegistry.listInstalledAddons();
	for (AddonSpecification addonSpec : addons) {
	    unloadAddon(addonSpec);
	}
    }

    /**
     * Unload all actions and protocols of a specific add-on.
     *
     * @param addonSpec The {@link AddonSpecification} of the add-on to unload.
     */
    protected void unloadAddon(AddonSpecification addonSpec) {
	TreeMap<String, IFDProtocol> ifdAddon = ifdProtocolCache.get(addonSpec);
	if (ifdAddon != null) {
	    for (IFDProtocol prot : ifdAddon.values()) {
		prot.destroy();
	    }
	    ifdProtocolCache.remove(addonSpec);
	}

	TreeMap<String, SALProtocol> salAddon = salProtocolCache.get(addonSpec);
	if (salAddon != null) {
	    for (SALProtocol prot : salAddon.values()) {
		prot.destroy();
	    }
	    salProtocolCache.remove(addonSpec);
	}

	TreeMap<String, AppExtensionAction> appExtActionAddon = appExtActionCache.get(addonSpec);
	if (appExtActionAddon != null) {
	    for (AppExtensionAction action : appExtActionAddon.values()) {
		action.destroy();
	    }
	    appExtActionCache.remove(addonSpec);
	}

	TreeMap<String, AppPluginAction> appPluginActionAddon = appPluginActionCache.get(addonSpec);
	if (appPluginActionAddon != null) {
	    for (AppPluginAction appPlugAction : appPluginActionAddon.values()) {
		appPlugAction.destroy();
	    }
	    appPluginActionCache.remove(addonSpec);
	}
    }


    /**
     * This method returns an instance of the given registry where only the interface methods are accessible.
     *
     * @param registry Unprotected registry instance.
     * @return Protected registry instance.
     */
    private static AddonRegistry getProtectedRegistry(AddonRegistry registry) {
	ClassLoader cl = AddonManager.class.getClassLoader();
	Class<?>[] interfaces = new Class<?>[] { AddonRegistry.class };
	InvocationHandler handler = new FacadeInvocationHandler(registry);
	Object o = Proxy.newProxyInstance(cl, interfaces, handler);
	return (AddonRegistry) o;
    }

    /**
     * Get the CombiningRigistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link CombiningRegistry}.
     */
    public AddonRegistry getRegistry() {
	return protectedRegistry;
    }

    /**
     * Get the ClasspathRegistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link ClasspathRegistry}.
     */
    public AddonRegistry getBuiltinRegistry() {
	return getProtectedRegistry(registry.getClasspathRegistry());
    }

    /**
     * Get the FileRegistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link FileRegistry}.
     */
    public AddonRegistry getExternalRegistry() {
	return getProtectedRegistry(registry.getFileRegistry());
    }

    /**
     * Register a new add-on which is located in the class path.
     *
     * @param desc {@link AddonSpecification} of the add-on which shall be registered.
     */
    public void registerClasspathAddon(AddonSpecification desc) {
	// TODO: protect this method from the sandbox
	this.registry.getClasspathRegistry().register(desc);
    }

    /**
     * Get a specific IFDProtocol.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link IFDProtocol}.
     * @param uri The {@link ProtocolPluginSpecification#uri} to identify the requested IFDProtocol.
     * @return The requested IFDProtocol object or NULL if no such object was found.
     */
    public IFDProtocol getIFDProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	TreeMap<String, IFDProtocol> addon = ifdProtocolCache.get(addonSpec);

	if (addon != null) {
	    // addon spec cached and has IFDProtocols so check whether we have the one requested
	    IFDProtocol ifdProt = addon.get(uri);
	    if (ifdProt != null) {
		// protocol cached so return it
		return ifdProt;
	    }
	}

	ProtocolPluginSpecification protoSpec = addonSpec.searchIFDActionByURI(uri);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	IFDProtocolProxy protoFactory = new IFDProtocolProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    if (addon == null) {
		TreeMap<String, IFDProtocol> cacheEntry = new TreeMap<>();
		cacheEntry.put(uri, protoFactory);
		ifdProtocolCache.put(addonSpec, cacheEntry);
	    } else {
		addon.put(uri, protoFactory);
		ifdProtocolCache.put(addonSpec, addon);
	    }
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of IFD Protocol failed", e);
	}
	return null;
    }

    /**
     * Get a specific SALProtocol.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link SALProtocol}.
     * @param uri The {@link ProtocolPluginSpecification#uri} to identify the requested SALProtocol.
     * @return The requested SALProtocol object or NULL if no such object was found.
     */
    public SALProtocol getSALProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	TreeMap<String, SALProtocol> addon = salProtocolCache.get(addonSpec);

	if (addon != null) {
	    // addon spec cached and has SALProtocols so check whether we have the one requested
	    SALProtocol salProt = addon.get(uri);
	    if (salProt != null) {
		// protocol cached so return it
		return salProt;
	    }
	}

	ProtocolPluginSpecification protoSpec = addonSpec.searchSALActionByURI(uri);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	SALProtocolProxy protoFactory = new SALProtocolProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    if (addon == null) {
		TreeMap<String, SALProtocol> cacheEntry = new TreeMap<>();
		cacheEntry.put(uri, protoFactory);
		salProtocolCache.put(addonSpec, cacheEntry);
	    } else {
		addon.put(uri, protoFactory);
		salProtocolCache.put(addonSpec, addon);
	    }
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of SAL Protocol failed", e);
	}
	return null;
    }

    /**
     * Get a specific AppExtensionAction.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link AppExtensionAction}.
     * @param actionId	The {@link AppExtensionSpecification#id} to identify the requested AppExtensionAction.
     * @return The AppExtensionAction which corresponds the given {@code actionId} or NULL if no AppExtensionAction with
     * the given {@code actionId} exists.
     */
    public AppExtensionAction getAppExtensionAction(@Nonnull AddonSpecification addonSpec, @Nonnull String actionId) {
	TreeMap<String, AppExtensionAction> addon = appExtActionCache.get(addonSpec);

	if (addon != null) {
	    // addon spec cached and has AppExtensionAction so check whether we have the one requested
	    AppExtensionAction appExtAction = addon.get(actionId);
	    if (appExtAction != null) {
		// AppExtensionAction cached so return it
		return appExtAction;
	    }
	}

	AppExtensionSpecification protoSpec = addonSpec.searchByActionId(actionId);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	AppExtensionActionProxy protoFactory = new AppExtensionActionProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    if (addon == null) {
		TreeMap<String, AppExtensionAction> cacheEntry = new TreeMap<>();
		cacheEntry.put(actionId, protoFactory);
		appExtActionCache.put(addonSpec, cacheEntry);
	    } else {
		addon.put(actionId, protoFactory);
		appExtActionCache.put(addonSpec, addon);
	    }
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of AppExtensionAction failed", e);
	}
	return null;
    }

    /**
     * Get a specific AppPluginAction.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link AppPluginAction}.
     * @param resourceName The {@link AppPluginSpecification#resourceName} to identify the @{@link AppPluginAction} to
     * return.
     * @return A AppPluginAction which corresponds to the {@link AddonSpecification} and the {@code resourceName}. If no
     * such AppPluginAction exists NULL is returned.
     */
    public AppPluginAction getAppPluginAction(@Nonnull AddonSpecification addonSpec, @Nonnull String resourceName) {
	TreeMap<String, AppPluginAction> addon = appPluginActionCache.get(addonSpec);

	if (addon != null) {
	    // addon spec cached and has AppExtensionAction so check whether we have the one requested
	    AppPluginAction appPluginAction = addon.get(resourceName);
	    if (appPluginAction != null) {
		// AppExtensionAction cached so return it
		return appPluginAction;
	    }
	}

	AppPluginSpecification protoSpec = addonSpec.searchByResourceName(resourceName);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	AppPluginActionProxy protoFactory = new AppPluginActionProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    if (addon == null) {
		TreeMap<String, AppPluginAction> cacheEntry = new TreeMap<>();
		cacheEntry.put(resourceName, protoFactory);
		appPluginActionCache.put(addonSpec, cacheEntry);
	    } else {
		addon.put(resourceName, protoFactory);
		appPluginActionCache.put(addonSpec, addon);
	    }
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of AppPluginAction failed", e);
	}
	return null;
    }

    /**
     * Shut down the AddonManager.
     * The method unloads all installed add-ons.
     */
    public void shutdown() {
	unloadAllAddons();
    }

    /**
     * Uninstall an add-on.
     * This i primarily a wrapper method for the
     * {@link FileRegistry#uninstallAddon(org.openecard.addon.manifest.AddonSpecification)
     *
     * @param addonSpec The {@link AddonSpecification} of the add-on to uninstall.
     */
    public void uninstallAddon(@Nonnull AddonSpecification addonSpec) {
	// unloading is done by the PluginDirectoryAlterationListener
	registry.getFileRegistry().uninstallAddon(addonSpec);
    }

}
