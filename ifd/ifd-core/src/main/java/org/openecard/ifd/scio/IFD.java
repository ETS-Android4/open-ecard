/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.BioSensorCapabilityType;
import iso.std.iso_iec._24727.tech.schema.Cancel;
import iso.std.iso_iec._24727.tech.schema.CancelResponse;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationData;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationDataResponse;
import iso.std.iso_iec._24727.tech.schema.Output;
import iso.std.iso_iec._24727.tech.schema.OutputResponse;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.event.EventType;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.ifd.Protocol;
import org.openecard.common.ifd.ProtocolFactory;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.ifd.anytype.PACEOutputType;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.TerminalFactory;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.interfaces.Publish;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.ValueGenerators;
import org.openecard.ifd.event.IfdEventManager;
import org.openecard.ifd.scio.reader.EstablishPACERequest;
import org.openecard.ifd.scio.reader.EstablishPACEResponse;
import org.openecard.ifd.scio.reader.ExecutePACERequest;
import org.openecard.ifd.scio.reader.ExecutePACEResponse;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.openecard.ifd.scio.wrapper.ChannelManager;
import org.openecard.ifd.scio.wrapper.IFDTerminalFactory;
import org.openecard.ifd.scio.wrapper.NoSuchChannel;
import org.openecard.ifd.scio.wrapper.SingleThreadChannel;
import org.openecard.ifd.scio.wrapper.TerminalInfo;
import org.openecard.ws.common.GenericFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SCIO implementation of the IFD interface.
 *
 * @author Tobias Wich
 */
public class IFD implements org.openecard.ws.IFD {
    // TODO: make all commands cancellable

    private static final Logger LOG = LoggerFactory.getLogger(IFD.class);

    private byte[] ctxHandle = null;
    //private SCWrapper scwrapper;
    private ChannelManager cm;

    private Environment env;

    private final ProtocolFactories protocolFactories = new ProtocolFactories();
    private IfdEventManager evManager;

    private AtomicInteger numClients;
    private ExecutorService threadPool;
    private ConcurrentSkipListMap<String, Future<List<IFDStatusType>>> asyncWaitThreads;
    private Future<List<IFDStatusType>> syncWaitThread;
    private IFDTerminalFactory terminalFactoryBuilder;

    protected synchronized void removeAsnycTerminal(String session) {
	if (asyncWaitThreads != null) { // be sure the list still exists
	    asyncWaitThreads.remove(session);
	}
    }

    private boolean hasContext() {
	boolean hasContext = ctxHandle != null;
	return hasContext;
    }


    public void setEnvironment(Environment env) {
	this.env = env;
    }

    public boolean addProtocol(String proto, ProtocolFactory factory) {
	return protocolFactories.add(proto, factory);
    }

    private IFDTerminalFactory getTerminalFactoryBuilder() throws IFDException {
	IFDTerminalFactory currentBuilder = this.terminalFactoryBuilder;
	if (currentBuilder == null) {
	    currentBuilder = IFDTerminalFactory.configBackedInstance();
	    this.terminalFactoryBuilder = currentBuilder;
	}
	return currentBuilder;
    }

    public void setTerminalFactoryBuilder(IFDTerminalFactory terminalFactoryBuilder) {
	this.terminalFactoryBuilder = terminalFactoryBuilder;
    }

    @Override
    public synchronized EstablishContextResponse establishContext(EstablishContext parameters) {
	EstablishContextResponse response;
	try {
	    // on first call, create a new unique handle
	    if (ctxHandle == null) {
		TerminalFactory currentTermFactory = getTermFactory();
		cm = new ChannelManager(currentTermFactory);
		ctxHandle = ChannelManager.createCtxHandle();
		env.addIFDCtx(ctxHandle);
		numClients = new AtomicInteger(1);
		// TODO: add custom ThreadFactory to control the thread name
		threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
		    private final AtomicInteger num = new AtomicInteger(0);
		    private final ThreadGroup group = new ThreadGroup("IFD Wait");
		    @Override
		    public Thread newThread(Runnable r) {
			String name = String.format("SCIO Watcher %d", num.getAndIncrement());
			Thread t = new Thread(group, r, name);
			t.setDaemon(false);
			return t;
		    }
		});
		asyncWaitThreads = new ConcurrentSkipListMap<>();
		evManager = new IfdEventManager(env, ctxHandle);
		evManager.initialize();
	    } else {
		// on second or further calls, increment usage counter
		numClients.incrementAndGet();
	    }

	    // prepare response
	    response = WSHelper.makeResponse(EstablishContextResponse.class, WSHelper.makeResultOK());
	    response.setContextHandle(ctxHandle);
	    return response;
	} catch (IFDException ex) {
	    LOG.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(EstablishContextResponse.class, ex.getResult());
	}
    }

    private TerminalFactory getTermFactory() throws IFDException {
	IFDTerminalFactory factoryBuilder = this.getTerminalFactoryBuilder();
	TerminalFactory currentTermFactory;
	try {
	    currentTermFactory = factoryBuilder.getInstance();
	} catch (GenericFactoryException ex) {
	    throw new IFDException(ex);
	}
	return currentTermFactory;
    }

    @Override
    public synchronized ReleaseContextResponse releaseContext(ReleaseContext parameters) {
	ReleaseContextResponse response;
	if (ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    if (numClients.decrementAndGet() == 0) { // last client detaches
		env.removeIFDCtx(ctxHandle);
		ctxHandle = null;
		numClients = null;
		// terminate thread pool
		threadPool.shutdownNow(); // wait for threads to die and block new requests
		// just assume it worked ... and don't wait
		threadPool = null;
		asyncWaitThreads = null;
	    }
	    evManager.terminate();

	    response = WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResultOK());
	    return response;
	} else {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(ReleaseContextResponse.class, r);
	    return response;
	}
    }

    @Override
    public PrepareDevicesResponse prepareDevices(PrepareDevices parameters) {
	boolean wasPrepared;
	try {
	    wasPrepared = cm.prepareDevices();
	} catch (SCIOException ex) {
	    String minorError;
	    switch (ex.getCode()) {
	    	case SCARD_W_CANCELLED_BY_USER:
		    minorError = ECardConstants.Minor.IFD.CANCELLATION_BY_USER;
		    break;
	    	case SCARD_E_TIMEOUT:
		    minorError =  ECardConstants.Minor.IFD.Terminal.WAIT_FOR_DEVICE_TIMEOUT;
		    break;
	    	default:
		    minorError = ECardConstants.Minor.IFD.Terminal.PREPARE_DEVICES_ERROR;
		    break;
	    }
	    Result r = WSHelper.makeResultError(minorError, ex.getMessage());
	    return WSHelper.makeResponse(PrepareDevicesResponse.class, r);
	}

	if (wasPrepared) {
	    ConnectionHandleType handle = HandlerBuilder.create()
		    .setContextHandle(parameters.getContextHandle())
		    .buildConnectionHandle();

	    env.getEventDispatcher().notify(EventType.PREPARE_DEVICES, new IfdEventObject(handle));
	}

	return WSHelper.makeResponse(PrepareDevicesResponse.class, WSHelper.makeResultOK());
    }

    @Override
    public PowerDownDevicesResponse powerDownDevices(PowerDownDevices parameters) {
	boolean wasPoweredDown = cm.powerDownDevices();

	if (wasPoweredDown) {
	    ConnectionHandleType handle = HandlerBuilder.create()
		    .setContextHandle(parameters.getContextHandle())
		    .buildConnectionHandle();

	    env.getEventDispatcher().notify(EventType.POWER_DOWN_DEVICES, new IfdEventObject(handle));
	}

	return WSHelper.makeResponse(PowerDownDevicesResponse.class, WSHelper.makeResultOK());
    }

    @Override
    public ListIFDsResponse listIFDs(ListIFDs parameters) {
	ListIFDsResponse response;
	if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(ListIFDsResponse.class, r);
	    return response;
	} else {
	    try {
		List<SCIOTerminal> terminals = cm.getTerminals().list();
		ArrayList<String> ifds = new ArrayList<>(terminals.size());
		for (SCIOTerminal next : terminals) {
		    ifds.add(next.getName());
		}
		response = WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResultOK());
		response.getIFDName().addAll(ifds);
		return response;
	    } catch (SCIOException ex) {
		LOG.warn(ex.getMessage(), ex);
		Result r = WSHelper.makeResultUnknownIFDError(ex.getMessage());
		response = WSHelper.makeResponse(ListIFDsResponse.class, r);
		return response;
	    }
	}
    }


    @Override
    public GetIFDCapabilitiesResponse getIFDCapabilities(GetIFDCapabilities parameters) {
	GetIFDCapabilitiesResponse response;

	// you thought of a different IFD obviously
	if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, r);
	    return response;
	}

	try {
	    TerminalInfo info;
	    String ifdName = parameters.getIFDName();
	    try {
		SingleThreadChannel channel = cm.openMasterChannel(ifdName);
		info = new TerminalInfo(cm, channel);
	    } catch (NoSuchTerminal ex) {
		// continue without a channel
		SCIOTerminal term = cm.getTerminals().getTerminal(ifdName);
		info = new TerminalInfo(cm, term);
	    }

	    IFDCapabilitiesType cap = new IFDCapabilitiesType();

	    // slot capability
	    SlotCapabilityType slotCap = info.getSlotCapability();
	    cap.getSlotCapability().add(slotCap);
	    // ask protocol factory which types it supports
	    List<String> protocols = slotCap.getProtocol();
	    for (String proto : protocolFactories.protocols()) {
		if (! protocols.contains(proto)) {
		    protocols.add(proto);
		}
	    }
	    // add built in protocols stuff
	    // TODO: PIN Compare should be a part of establishChannel and thus just appear in the software protocol list
	    if (! protocols.contains(ECardConstants.Protocol.PIN_COMPARE)){
		protocols.add(ECardConstants.Protocol.PIN_COMPARE);
	    }

	    // display capability
	    DisplayCapabilityType dispCap = info.getDisplayCapability();
	    if (dispCap != null) {
		cap.getDisplayCapability().add(dispCap);
	    }

	    // keypad capability
	    KeyPadCapabilityType keyCap = info.getKeypadCapability();
	    if (keyCap != null) {
		cap.getKeyPadCapability().add(keyCap);
	    }

	    // biosensor capability
	    BioSensorCapabilityType bioCap = info.getBiosensorCapability();
	    if (bioCap != null) {
		cap.getBioSensorCapability().add(bioCap);
	    }

	    // acoustic and optical elements
	    cap.setOpticalSignalUnit(info.isOpticalSignal());
	    cap.setAcousticSignalUnit(info.isAcousticSignal());

	    // prepare response
	    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, WSHelper.makeResultOK());
	    response.setIFDCapabilities(cap);
	    return response;
	} catch (NullPointerException | NoSuchTerminal ex) {
	    String msg = String.format("Requested terminal not found.");
	    LOG.warn(msg, ex);
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
	    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, r);
	    return response;
	} catch (SCIOException ex) {
	    String msg = String.format("Failed to request status from terminal.");
	    // use debug when card has been removed, as this happens all the time
	    SCIOErrorCode code = ex.getCode();
	    if (! (code == SCIOErrorCode.SCARD_E_NO_SMARTCARD || code == SCIOErrorCode.SCARD_W_REMOVED_CARD)) {
		LOG.warn(msg, ex);
	    } else {
		LOG.debug(msg, ex);
	    }
	    Result r = WSHelper.makeResultUnknownIFDError(msg);
	    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, r);
	    return response;
	} catch (InterruptedException ex) {
	    String msg = String.format("Cancellation by user.");
	    LOG.warn(msg, ex);
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
	    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, r);
	    return response;
	}
    }


    @Override
    public GetStatusResponse getStatus(GetStatus parameters) {
	GetStatusResponse response;

	// you thought of a different IFD obviously
	if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(GetStatusResponse.class, r);
	    return response;
	}

	// get specific ifd or all if no specific one is requested
	List<SCIOTerminal> ifds = new LinkedList<>();
	try {
	    String requestedIfd = parameters.getIFDName();
	    if (requestedIfd != null) {
		try {
		    SCIOTerminal t = cm.getTerminals().getTerminal(requestedIfd);
		    ifds.add(t);
		} catch (NoSuchTerminal ex) {
		    String msg = "The requested IFD name does not exist.";
		    LOG.warn(msg, ex);
		    String minor = ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD;
		    Result r = WSHelper.makeResult(ECardConstants.Major.ERROR, minor, msg);
		    response = WSHelper.makeResponse(GetStatusResponse.class, r);
		    return response;
		}
	    } else {
		ifds.addAll(cm.getTerminals().list());
	    }
	} catch (SCIOException ex) {
	    String msg = "Failed to get list with the terminals.";
	    LOG.warn(msg, ex);
	    response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResultUnknownIFDError(msg));
	    return response;
	}

	// request status for each ifd
	ArrayList<IFDStatusType> status = new ArrayList<>(ifds.size());
	for (SCIOTerminal ifd : ifds) {

	    TerminalInfo info;
	    try {
		SingleThreadChannel channel = cm.openMasterChannel(ifd.getName());
		info = new TerminalInfo(cm, channel);
	    } catch (NoSuchTerminal | SCIOException ex) {
		// continue without a channel
		info = new TerminalInfo(cm, ifd);
	    }
	    try {
		IFDStatusType s = info.getStatus();
		status.add(s);
	    } catch (SCIOException ex) {
		if (ex.getCode() != SCIOErrorCode.SCARD_W_UNPOWERED_CARD &&
			ex.getCode() != SCIOErrorCode.SCARD_W_UNRESPONSIVE_CARD &&
			ex.getCode() != SCIOErrorCode.SCARD_W_UNSUPPORTED_CARD &&
			ex.getCode() != SCIOErrorCode.SCARD_E_PROTO_MISMATCH) {
		    String msg = String.format("Failed to determine status of terminal '%s'.", ifd.getName());
		    LOG.warn(msg, ex);
		    Result r = WSHelper.makeResultUnknownIFDError(msg);
		    response = WSHelper.makeResponse(GetStatusResponse.class, r);
		    return response;
		} else {
		    // fall througth if there is a card which can not be connected
		    LOG.info("Ignoring failed status request from terminal.", ex);
		}
	    }
	}

	// everything worked out well
	response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResultOK());
	response.getIFDStatus().addAll(status);
	return response;
    }


    @Override
    public WaitResponse wait(Wait parameters) {
	WaitResponse response;

	// you thought of a different IFD obviously
	if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(WaitResponse.class, r);
	    return response;
	}

	// get timeout value
	BigInteger timeout = parameters.getTimeOut();
	if (timeout == null) {
	    timeout = BigInteger.valueOf(Long.MAX_VALUE);
	}
	if (timeout.signum() == -1 || timeout.signum() == 0) {
	    String msg = "Invalid timeout value given, must be strictly positive.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg);
	    response = WSHelper.makeResponse(WaitResponse.class, r);
	    return response;
	}
	long timeoutL;
	try {
	    timeoutL = (long) timeout.doubleValue();
	} catch (ArithmeticException ex) {
	    LOG.warn("Too big timeout value give, shortening to Long.MAX_VALUE.");
	    timeoutL = Long.MAX_VALUE;
	}

	ChannelHandleType callback = parameters.getCallback();
	// callback is only useful with a protocol termination point
	if (callback != null && callback.getProtocolTerminationPoint() == null) {
	    callback = null;
	}

	// if callback, generate session id
	String sessionId = null;
	if (callback != null) {
	    ChannelHandleType newCallback = new ChannelHandleType();
	    newCallback.setBinding(callback.getBinding());
	    newCallback.setPathSecurity(callback.getPathSecurity());
	    newCallback.setProtocolTerminationPoint(callback.getProtocolTerminationPoint());
	    sessionId = ValueGenerators.genBase64Session();
	    newCallback.setSessionIdentifier(sessionId);
	    callback = newCallback;
	}

	try {
	    EventWatcher watcher = new EventWatcher(cm, timeoutL, callback);
	    List<IFDStatusType> initialState = watcher.start();

	    // get expected status or initial status for all if none specified
	    List<IFDStatusType> expectedState = parameters.getIFDStatus();
	    if (expectedState.isEmpty()) {
		expectedState = initialState;
	    } else {
		for (IFDStatusType s : expectedState) {
		    // check that ifdname is present, needed for comparison
		    if (s.getIFDName() == null) {
			String msg = "IFD in a request IFDStatus not known.";
			Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
			response = WSHelper.makeResponse(WaitResponse.class, r);
			return response;
		    }
		    // check that at least one slot entry is present
		    if (s.getSlotStatus().isEmpty()) {
			// assume an empty one
			SlotStatusType slot = new SlotStatusType();
			slot.setCardAvailable(false);
			slot.setIndex(BigInteger.ZERO);
			s.getSlotStatus().add(slot);
		    }
		}
	    }
	    watcher.setExpectedState(expectedState);

	    // create the future and fire
	    FutureTask<List<IFDStatusType>> future = new FutureTask<>(watcher);
	    if (watcher.isAsync()) {
		// add future to async wait list
		asyncWaitThreads.put(sessionId, future);
		threadPool.execute(future); // finally run this darn thingy

		// prepare result with session id in it
		response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultOK());
		response.setSessionIdentifier(sessionId);
		return response;
	    } else {
		// run wait in a future so it can be easily interrupted
		syncWaitThread = future;
		threadPool.execute(future);

		// get results from the future
		List<IFDStatusType> events = future.get();

		// prepare response
		response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultOK());
		response.getIFDEvent().addAll(events);
		return response;
	    }
	} catch (SCIOException ex) {
	    String msg = "Unknown SCIO error occured during wait call.";
	    LOG.warn(msg, ex);
	    Result r = WSHelper.makeResultUnknownIFDError(msg);
	    if (ex.getCode() == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
		r.setResultMinor(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE);
	    }
	    response = WSHelper.makeResponse(WaitResponse.class, r);
	    return response;
	} catch (ExecutionException ex) { // this is the exception from within the future
	    Throwable cause = ex.getCause();
	    if (cause instanceof SCIOException) {
		String msg = "Unknown SCIO error occured during wait call.";
		LOG.warn(msg, cause);
		Result r = WSHelper.makeResultUnknownIFDError(msg);
		if (((SCIOException) ex.getCause()).getCode() == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
		    r.setResultMinor(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE);
		}
		response = WSHelper.makeResponse(WaitResponse.class, r);
	    } else {
		String msg = "Unknown error during wait call.";
		LOG.error(msg, cause);
		Result r = WSHelper.makeResultUnknownError(msg);
		response =  WSHelper.makeResponse(WaitResponse.class, r);
	    }
	    return response;
	} catch (InterruptedException ex) {
	    String msg = "Wait interrupted by another thread.";
	    LOG.warn(msg, ex);
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
	    response = WSHelper.makeResponse(WaitResponse.class, r);
	    Thread.currentThread().interrupt();
	    return response;
	}
    }


    @Override
    public CancelResponse cancel(Cancel parameters) {
	CancelResponse response;

	// you thought of a different IFD obviously
	if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
	    String msg = "Invalid context handle specified.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
	    response = WSHelper.makeResponse(CancelResponse.class, r);
	    return response;
	}

	String ifdName = parameters.getIFDName();
	String session = parameters.getSessionIdentifier();
	if (session != null) {
	    // async wait
	    Future<List<IFDStatusType>> f = this.asyncWaitThreads.get(session);
	    if (f != null) {
		f.cancel(true);
		response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultOK());
	    } else {
		String msg = "No matching Wait call exists for the given session.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, msg);
		response = WSHelper.makeResponse(CancelResponse.class, r);
	    }
	} else if (ifdName != null) {
	    // TODO: kill only if request is specific to the named terminal
	    // sync wait
	    synchronized (this) {
		if (syncWaitThread != null) {
		    syncWaitThread.cancel(true);
		    syncWaitThread = null; // not really needed but seems cleaner
		    response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultOK());
		} else {
		    String msg = "No synchronous Wait to cancel.";
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, msg);
		    response = WSHelper.makeResponse(CancelResponse.class, r);
		}
	    }
	} else {
	    // nothing to cancel
	    String msg = "Invalid parameters given.";
	    response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultUnknownError(msg));
	}

	return response;
    }


    /**
     * Note: the first byte of the command data is the control code.
     */
    @Override
    public ControlIFDResponse controlIFD(ControlIFD parameters) {
	ControlIFDResponse response;

	if (! hasContext()) {
	    String msg = "Context not initialized.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
	    response = WSHelper.makeResponse(ControlIFDResponse.class, r);
	    return response;
	}

	byte[] handle = parameters.getSlotHandle();
	byte[] command = parameters.getCommand();
	if (handle == null || command == null) {
	    String msg = "Missing parameter.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.PARM_ERROR, msg);
	    response = WSHelper.makeResponse(ControlIFDResponse.class, r);
	    return response;
	}
	byte ctrlCode = command[0];
	command = Arrays.copyOfRange(command, 1, command.length);

	try {
	    SingleThreadChannel ch = cm.getSlaveChannel(handle);
	    TerminalInfo info = new TerminalInfo(cm, ch);
	    Integer featureCode = info.getFeatureCodes().get(Integer.valueOf(ctrlCode));
	    // see if the terminal can deal with that
	    if (featureCode != null) {
		byte[] resultCommand = ch.transmitControlCommand(featureCode, command);

		// evaluate result
		Result result = evaluateControlIFDRAPDU(resultCommand);
		response = WSHelper.makeResponse(ControlIFDResponse.class, result);
		response.setResponse(resultCommand);
		return response;
	    } else {
		String msg = "The terminal is not capable of performing the requested action.";
		Result r = WSHelper.makeResultUnknownIFDError(msg);
		response = WSHelper.makeResponse(ControlIFDResponse.class, r);
		return response;
	    }
	} catch (NoSuchChannel | IllegalStateException ex) {
	    String msg = "The card or the terminal is not available anymore.";
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
	    response = WSHelper.makeResponse(ControlIFDResponse.class, r);
	    LOG.warn(msg, ex);
	    return response;
	} catch (SCIOException ex) {
	    String msg = "Unknown error while sending transmit control command.";
	    Result r = WSHelper.makeResultUnknownIFDError(msg);
	    response = WSHelper.makeResponse(ControlIFDResponse.class, r);
	    LOG.warn(msg, ex);
	    return response;
	} catch (InterruptedException ex) {
	    String msg = String.format("Cancellation by user.");
	    LOG.warn(msg, ex);
	    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
	    response = WSHelper.makeResponse(ControlIFDResponse.class, r);
	    return response;
	}
    }


    @Override
    public ConnectResponse connect(Connect parameters) {
	try {
	    ConnectResponse response;
	    // check if the requested handle is valid
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(ConnectResponse.class, r);
		return response;
	    } else {
		try {
		    String name = parameters.getIFDName();

		    // make sure the slot is connected before attemting to get a slave channel
		    cm.openMasterChannel(name);

		    byte[] slotHandle = cm.openSlaveChannel(name).p1;
		    SingleThreadChannel ch = cm.getSlaveChannel(slotHandle);

		    // make connection exclusive
		    Boolean exclusive = parameters.isExclusive();
		    if (exclusive != null && exclusive == true) {
			BeginTransaction transact = new BeginTransaction();
			transact.setSlotHandle(slotHandle);
			BeginTransactionResponse resp = beginTransaction(transact);
			if (resp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
			    // destroy channel, when not successful here
			    ch.shutdown();
			    response = WSHelper.makeResponse(ConnectResponse.class, resp.getResult());
			    return response;
			}
		    }

		    // connection established, return result
		    response = WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResultOK());
		    response.setSlotHandle(slotHandle);
		    return response;
		} catch (NoSuchTerminal | NullPointerException ex) {
		    String msg = "The requested terminal does not exist.";
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
		    response = WSHelper.makeResponse(ConnectResponse.class, r);
		    LOG.warn(msg, ex);
		    return response;
		} catch (IllegalStateException ex) {
		    String msg = "No card available in the requested terminal.";
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.NO_CARD, msg);
		    response = WSHelper.makeResponse(ConnectResponse.class, r);
		    LOG.warn(msg, ex);
		    return response;
		} catch (SCIOException ex) {
		    String msg = "Unknown error in the underlying SCIO implementation.";
		    Result r = WSHelper.makeResultUnknownIFDError(msg);
		    response = WSHelper.makeResponse(ConnectResponse.class, r);
		    LOG.warn(msg, ex);
		    return response;
		}
	    }
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public synchronized DisconnectResponse disconnect(Disconnect parameters) {
	try {
	    DisconnectResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(DisconnectResponse.class, r);
		return response;
	    }

	    try {
		byte[] handle = parameters.getSlotHandle();
		SingleThreadChannel ch = cm.getSlaveChannel(handle);

		if (ch.isTransacted()) {
		    ch.endExclusive();
		}

		cm.closeSlaveChannel(handle);

		// process actions
		SCIOCard card = ch.getChannel().getCard();
		ActionType action = parameters.getAction();
		if (ActionType.RESET == action) {
		    String ifdName = card.getTerminal().getName();
		    SingleThreadChannel master = cm.getMasterChannel(ifdName);

		    HandlerBuilder builder = HandlerBuilder.create();
		    ConnectionHandleType cHandleIn = builder.setCardType(ECardConstants.UNKNOWN_CARD)
			    .setCardIdentifier(card.getATR().getBytes())
			    .setContextHandle(ctxHandle)
			    .setIfdName(ifdName)
			    .setSlotIdx(BigInteger.ZERO)
			    .buildConnectionHandle();
		    builder = HandlerBuilder.create();
		    ConnectionHandleType cHandleRm = builder.setContextHandle(ctxHandle)
			    .setIfdName(ifdName)
			    .setSlotIdx(BigInteger.ZERO)
			    .buildConnectionHandle();

		    try {
			master.reconnect();
			evManager.emitResetCardEvent(cHandleRm, cHandleIn, card.getProtocol().toUri());
		    } catch (IllegalStateException ex) {
			LOG.warn("Card reconnect failed, trying to establish new card connection.", ex);
			cm.closeMasterChannel(ifdName);
			LOG.debug("Master channel closed successfully.");
			try {
			    cm.getMasterChannel(ifdName);
			    LOG.debug("New card connection established successfully.");
			    evManager.emitResetCardEvent(cHandleRm, cHandleIn, card.getProtocol().toUri());
			} catch (NoSuchTerminal ex2) {
			    LOG.error("No terminal present anymore.", ex);
			}
		    }
		}
		// TODO: take care of other actions (probably over ControlIFD)
		// the default is to not disconnect the card, because all existing connections would be broken

		response = WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResultOK());
		return response;
	    } catch (NoSuchChannel ex) {
		String msg = "No card available in the requested terminal.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(DisconnectResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    } catch (SCIOException ex) {
		String msg = "Unknown error in the underlying SCIO implementation.";
		Result r = WSHelper.makeResultUnknownIFDError(msg);
		response = WSHelper.makeResponse(DisconnectResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    }
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public BeginTransactionResponse beginTransaction(BeginTransaction beginTransaction) {
	try {
	    BeginTransactionResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(BeginTransactionResponse.class, r);
		return response;
	    }

	    try {
		byte[] handle = beginTransaction.getSlotHandle();
		SingleThreadChannel ch = cm.getSlaveChannel(handle);
		ch.beginExclusive();
	    } catch (NoSuchChannel | IllegalStateException ex) {
		String msg = "No card available in the requested terminal.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(BeginTransactionResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    } catch (SCIOException ex) {
		String msg;
		String minor;
		switch (ex.getCode()) {
		    case SCARD_W_RESET_CARD:
		    case SCARD_W_REMOVED_CARD:
		    case SCARD_E_READER_UNAVAILABLE:
		    case SCARD_E_NO_SMARTCARD:
		    case SCARD_E_NO_SERVICE:
			msg = String.format("Slot handle is not available [%s].", ex.getCode().name());
			minor = ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE;
			LOG.debug(msg, ex);
			break;
		    default:
			msg = "Unknown error in the underlying SCIO implementation.";
			minor = ECardConstants.Minor.App.UNKNOWN_ERROR;
			LOG.warn(msg, ex);
		}
		Result r = WSHelper.makeResultError(minor, msg);
		response = WSHelper.makeResponse(BeginTransactionResponse.class, r);
		return response;
	    }

	    response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResultOK());
	    return response;
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResult(ex));
	}
    }

    @Override
    public EndTransactionResponse endTransaction(EndTransaction parameters) {
	try {
	    EndTransactionResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(EndTransactionResponse.class, r);
		return response;
	    }

	    try {
		byte[] handle = parameters.getSlotHandle();
		SingleThreadChannel ch = cm.getSlaveChannel(handle);
		ch.endExclusive();
	    } catch (NoSuchChannel | IllegalStateException ex) {
		String msg = "No card with transaction available in the requested terminal.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(EndTransactionResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    } catch (SCIOException ex) {
		String msg = "Unknown error in the underlying SCIO implementation.";
		Result r = WSHelper.makeResultUnknownIFDError(msg);
		response = WSHelper.makeResponse(EndTransactionResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    }

	    response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResultOK());
	    return response;
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Publish
    @Override
    public TransmitResponse transmit(Transmit parameters) {
	try {
	    TransmitResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(TransmitResponse.class, r);
		return response;
	    }

	    try {
		byte[] handle = parameters.getSlotHandle();
		SingleThreadChannel ch = cm.getSlaveChannel(handle);

		List<InputAPDUInfoType> apdus = parameters.getInputAPDUInfo();
		// check that the apdus contain sane values
		for (InputAPDUInfoType apdu : apdus) {
		    for (byte[] code : apdu.getAcceptableStatusCode()) {
			if (code.length == 0 || code.length > 2) {
			    String msg = "Invalid accepted status code given.";
			    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.PARM_ERROR, msg);
			    response = WSHelper.makeResponse(TransmitResponse.class, r);
			    return response;
			}
		    }
		}

		// transmit APDUs and stop if an error occurs or a not expected status is hit
		response = WSHelper.makeResponse(TransmitResponse.class, WSHelper.makeResultOK());
		Result result;
		List<byte[]> rapdus = response.getOutputAPDU();
		try {
		    for (InputAPDUInfoType capdu : apdus) {
			byte[] rapdu = ch.transmit(capdu.getInputAPDU(), capdu.getAcceptableStatusCode());
			rapdus.add(rapdu);
		    }
		    result = WSHelper.makeResultOK();
		} catch (TransmitException ex) {
		    rapdus.add(ex.getResponseAPDU());
		    result = ex.getResult();
		} catch (SCIOException ex) {
		    String msg = "Error during transmit.";
		    LOG.warn(msg, ex);
		    result = WSHelper.makeResultUnknownIFDError(msg);
		} catch (IllegalStateException ex) {
		    String msg = "Card removed during transmit.";
		    LOG.warn(msg, ex);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		} catch (IllegalArgumentException ex) {
		    String msg = "Given command contains a MANAGE CHANNEL APDU.";
		    LOG.error(msg, ex);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		} catch (InterruptedException ex) {
		    String msg = String.format("Cancellation by user.");
		    LOG.error(msg, ex);
		    result = WSHelper.makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg);
		}

		response.setResult(result);

		return response;
	    } catch (NoSuchChannel | IllegalStateException ex) {
		String msg = "No card with transaction available in the requested terminal.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(TransmitResponse.class, r);
		LOG.warn(msg, ex);
		return response;
	    }
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(TransmitResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public VerifyUserResponse verifyUser(VerifyUser parameters) {
	// TODO: convert to IFD Protocol
	try {
	    VerifyUserResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(VerifyUserResponse.class, r);
		return response;
	    }

	    SingleThreadChannel channel = cm.getSlaveChannel(parameters.getSlotHandle());
	    AbstractTerminal aTerm = new AbstractTerminal(this, cm, channel, env.getGUI(), ctxHandle, parameters.getDisplayIndex());
	    try {
		response = aTerm.verifyUser(parameters);
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(VerifyUserResponse.class, ex.getResult());
		return response;
	    }
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	    throwThreadKillException(ex);
	    return WSHelper.makeResponse(VerifyUserResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public ModifyVerificationDataResponse modifyVerificationData(ModifyVerificationData parameters) {
	ModifyVerificationDataResponse response;
	String msg = "Command not supported.";
	response = WSHelper.makeResponse(ModifyVerificationDataResponse.class, WSHelper.makeResultUnknownError(msg));
	return response;
    }


    @Override
    public OutputResponse output(Output parameters) {
	OutputResponse response;
	String msg = "Command not supported.";
	response = WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResultUnknownError(msg));
	return response;
    }


    @Override
    public EstablishChannelResponse establishChannel(EstablishChannel parameters) {
	byte[] slotHandle = parameters.getSlotHandle();
	try {
	    SingleThreadChannel channel = cm.getSlaveChannel(slotHandle);
	    TerminalInfo termInfo = new TerminalInfo(cm, channel);
	    DIDAuthenticationDataType protoParam = parameters.getAuthenticationProtocolData();
	    String protocol = protoParam.getProtocol();

	    // check if it is PACE and try to perform native implementation
	    // get pace capabilities
	    List<PACECapabilities.PACECapability> paceCapabilities = termInfo.getPACECapabilities();
	    List<String> supportedProtos = TerminalInfo.buildPACEProtocolList(paceCapabilities);
	    // check out if this actually a PACE request
	    // FIXME: check type of protocol

	    // i don't care which type is supported, i try it anyways
	    if (! supportedProtos.isEmpty() && supportedProtos.get(0).startsWith(protocol)) {
		// yeah, PACE seems to be supported by the reader, big win
		PACEInputType paceParam = new PACEInputType(protoParam);
		// extract variables needed for pace
		byte pinID = paceParam.getPINID();
		// optional elements
		byte[] chat = paceParam.getCHAT();
		String pin = paceParam.getPIN();
		byte[] certDesc = paceParam.getCertificateDescription();

		// prepare pace data structures
		EstablishPACERequest estPaceReq = new EstablishPACERequest(pinID, chat, null, certDesc); // TODO: add supplied PIN
		ExecutePACERequest  execPaceReq = new ExecutePACERequest(ExecutePACERequest.Function.EstablishPACEChannel, estPaceReq.toBytes());
		// see if PACE type demanded for this input value combination is supported
		// TODO: check if this additional check is really necessary
		if (estPaceReq.isSupportedType(paceCapabilities)) {
		    byte[] reqData = execPaceReq.toBytes();
		    LOG.debug("executeCtrlCode request: {}", ByteUtils.toHexString(reqData));
		    // execute pace
		    Map<Integer, Integer> features = termInfo.getFeatureCodes();
		    byte[] resData = channel.transmitControlCommand(features.get(PCSCFeatures.EXECUTE_PACE), reqData);
		    LOG.debug("Response of executeCtrlCode: {}", ByteUtils.toHexString(resData));
		    // evaluate response
		    ExecutePACEResponse execPaceRes = new ExecutePACEResponse(resData);
		    if (execPaceRes.isError()) {
			return WSHelper.makeResponse(EstablishChannelResponse.class, execPaceRes.getResult());
		    }
		    EstablishPACEResponse estPaceRes = new EstablishPACEResponse(execPaceRes.getData());
		    // get values and prepare response
		    PACEOutputType authDataResponse = paceParam.getOutputType();
		    // mandatory fields
		    authDataResponse.setRetryCounter(estPaceRes.getRetryCounter());
		    authDataResponse.setEFCardAccess(estPaceRes.getEFCardAccess());
		    // optional fields
		    if (estPaceRes.hasCurrentCAR()) {
			authDataResponse.setCurrentCAR(estPaceRes.getCurrentCAR());
		    }
		    if (estPaceRes.hasPreviousCAR()) {
			authDataResponse.setPreviousCAR(estPaceRes.getPreviousCAR());
		    }
		    if (estPaceRes.hasIDICC()) {
			authDataResponse.setIDPICC(estPaceRes.getIDICC());
		    }
		    // create response type and return
		    EstablishChannelResponse response = WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResultOK());
		    response.setAuthenticationProtocolData(authDataResponse.getAuthDataType());
		    return response;
		}
	    } // end native pace support

	    // check out available software protocols
	    if (this.protocolFactories.contains(protocol)) {
		ProtocolFactory factory = this.protocolFactories.get(protocol);
		Protocol protoImpl = factory.createInstance();
		EstablishChannelResponse response = protoImpl.establish(parameters, env.getDispatcher(), env.getGUI());
		// register protocol instance for secure messaging when protocol was processed successful
		if (response.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		    channel.addSecureMessaging(protoImpl);
		}
		return response;
	    }

	    // if this point is reached a native implementation is not present, try registered protocols
	    Result r = WSHelper.makeResultUnknownError("No such protocol available in this IFD.");
	    return WSHelper.makeResponse(EstablishChannelResponse.class, r);
	} catch (Throwable t) {
	    return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    public DestroyChannelResponse destroyChannel(DestroyChannel parameters) {
	try {
	    DestroyChannelResponse destroyChannelResponse = new DestroyChannelResponse();
	    byte[] slotHandle = parameters.getSlotHandle();
	    SingleThreadChannel channel = cm.getSlaveChannel(slotHandle);
	    TerminalInfo termInfo = new TerminalInfo(cm, channel);

	    // check if it is PACE and try to perform native implementation
	    // get pace capabilities
	    List<PACECapabilities.PACECapability> paceCapabilities = termInfo.getPACECapabilities();
	    if (paceCapabilities.contains(PACECapabilities.PACECapability.DestroyPACEChannel)) {
		ExecutePACERequest execPaceReq = new ExecutePACERequest(ExecutePACERequest.Function.DestroyPACEChannel);

		byte[] reqData = execPaceReq.toBytes();
		LOG.debug("executeCtrlCode request: {}", ByteUtils.toHexString(reqData));
		// execute pace
		Map<Integer, Integer> features = termInfo.getFeatureCodes();
		byte[] resData = channel.transmitControlCommand(features.get(PCSCFeatures.EXECUTE_PACE), reqData);
		LOG.debug("Response of executeCtrlCode: {}", ByteUtils.toHexString(resData));
		// evaluate response
		ExecutePACEResponse execPaceRes = new ExecutePACEResponse(resData);
		if (execPaceRes.isError()) {
		    destroyChannelResponse =  WSHelper.makeResponse(DestroyChannelResponse.class, execPaceRes.getResult());
		}
	    }

	    channel.removeSecureMessaging();

	    if (destroyChannelResponse.getResult() == null) {
		Result r = new Result();
		r.setResultMajor(ECardConstants.Major.OK);
		destroyChannelResponse.setResult(r);
	    }

	    return destroyChannelResponse;
	} catch (Throwable t) {
	    return WSHelper.makeResponse(DestroyChannelResponse.class, WSHelper.makeResult(t));
	}
    }

    private Result evaluateControlIFDRAPDU(byte[] resultCommand) {
	int result = ByteUtils.toInteger(resultCommand);
	switch (result) {
	    case 0x9000: return WSHelper.makeResultOK();
	    case 0x6400: return WSHelper.makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.");
            default:     return WSHelper.makeResultUnknownIFDError("Unknown return code from terminal.");
	}
    }

    private void throwThreadKillException(Exception ex) {
	Throwable cause;
	if (ex instanceof InvocationTargetExceptionUnchecked) {
	    cause = ex.getCause();
	} else {
	    cause = ex;
	}

	if (cause instanceof ThreadTerminateException) {
	    throw (RuntimeException) cause;
	} else if (cause instanceof InterruptedException) {
	    throw new ThreadTerminateException("Thread running inside SAL interrupted.", cause);
	} else if (cause instanceof RuntimeException) {
	    throw (RuntimeException) ex;
	}
    }

}
