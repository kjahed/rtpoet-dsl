package ca.jahed.rtpoet.dsl.libraries;

import ca.jahed.rtpoet.dsl.rt.Class;
import ca.jahed.rtpoet.dsl.rt.Model;
import ca.jahed.rtpoet.dsl.rt.Protocol;
import ca.jahed.rtpoet.dsl.rt.Signal;
import ca.jahed.rtpoet.dsl.scoping.RtImportUriGlobalScopeProvider;
import ca.jahed.rtpoet.papyrusrt.rts.protocols.RTMQTTProtocol;
import ca.jahed.rtpoet.papyrusrt.rts.protocols.RTTCPProtocol;
import ca.jahed.rtpoet.rtmodel.RTElement;
import ca.jahed.rtpoet.rtmodel.rts.classes.RTCapsuleId;
import ca.jahed.rtpoet.rtmodel.rts.classes.RTMessage;
import ca.jahed.rtpoet.rtmodel.rts.classes.RTTimerId;
import ca.jahed.rtpoet.rtmodel.rts.classes.RTTimespec;
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTFrameProtocol;
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTLogProtocol;
import ca.jahed.rtpoet.rtmodel.rts.protocols.RTTimingProtocol;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.HashMap;
import java.util.Map;

public class RTSLibrary {
    private static final URI RTS_LIBRARY_URI = URI.createURI(
            RtImportUriGlobalScopeProvider.class.getClassLoader()
                    .getResource("libraries/RTSLibrary.rt").toString()
    );

    private static boolean loaded = false;
    private static final Map<EObject, RTElement> rtsProtocols = new HashMap<>();
    private static final Map<EObject, RTElement> rtsClasses = new HashMap<>();
    private static final Map<EObject, RTElement> rtsSignals = new HashMap<>();

    public static void load(ResourceSet resourceSet) {
        if(loaded)
            return;

        Resource rtsLibrary = resourceSet.getResource(RTS_LIBRARY_URI, true);
        Model rtsModel = (Model) rtsLibrary.getContents().get(0);

        for (Protocol protocol : rtsModel.getProtocols()) {
            switch (protocol.getName()) {
                case "Log":
                    rtsProtocols.put(protocol, RTLogProtocol.INSTANCE);
                    break;

                case "Timing":
                    rtsProtocols.put(protocol, RTTimingProtocol.INSTANCE);
                    for (Signal signal : protocol.getSignals()) {
                        if ("timeout".equals(signal.getName())) {
                            rtsSignals.put(signal, RTTimingProtocol.INSTANCE.timeout());
                        }
                    }
                    break;

                case "Frame":
                    rtsProtocols.put(protocol, RTFrameProtocol.INSTANCE);
                    break;

                case "TCP":
                    rtsProtocols.put(protocol, RTTCPProtocol.INSTANCE);
                    for (Signal signal : protocol.getSignals()) {
                        if ("received".equals(signal.getName())) {
                            rtsSignals.put(signal, RTTCPProtocol.INSTANCE.received());
                        } else if ("error".equals(signal.getName())) {
                            rtsSignals.put(signal, RTTCPProtocol.INSTANCE.error());
                        }  else if ("connected".equals(signal.getName())) {
                            rtsSignals.put(signal, RTTCPProtocol.INSTANCE.connected());
                        } else if ("disconnected".equals(signal.getName())) {
                            rtsSignals.put(signal, RTTCPProtocol.INSTANCE.disconnected());
                        }
                    }
                    break;

                case "MQTT":
                    rtsProtocols.put(protocol, RTMQTTProtocol.INSTANCE);
                    for (Signal signal : protocol.getSignals()) {
                        if ("received".equals(signal.getName())) {
                            rtsSignals.put(signal, RTMQTTProtocol.INSTANCE.received());
                        } else if ("error".equals(signal.getName())) {
                            rtsSignals.put(signal, RTMQTTProtocol.INSTANCE.error());
                        }  else if ("connected".equals(signal.getName())) {
                            rtsSignals.put(signal, RTMQTTProtocol.INSTANCE.connected());
                        } else if ("disconnected".equals(signal.getName())) {
                            rtsSignals.put(signal, RTMQTTProtocol.INSTANCE.disconnected());
                        }
                    }
                    break;
            }
        }

        for (Class clazz : rtsModel.getClasses()) {
            switch (clazz.getName()) {
                case "Message":
                    rtsClasses.put(clazz, RTMessage.INSTANCE);
                    break;
                case "CapsuleId":
                    rtsClasses.put(clazz, RTCapsuleId.INSTANCE);
                    break;
                case "TimerId":
                    rtsClasses.put(clazz, RTTimerId.INSTANCE);
                    break;
                case "Timespec":
                    rtsClasses.put(clazz, RTTimespec.INSTANCE);
                    break;
            }
        }

        loaded = true;
    }

    public static Map<EObject, RTElement> getRtsProtocols() {
        return rtsProtocols;
    }

    public static Map<EObject, RTElement> getRtsClasses() {
        return rtsClasses;
    }

    public static Map<EObject, RTElement> getRtsSignals() {
        return rtsSignals;
    }

    public static URI getURI() {
        return RTS_LIBRARY_URI;
    }

    private RTSLibrary() { }
}
