package hypercompliancelab.school.process.logging;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/***
 * This class is used to write XES log files.
 */
public class XesLogger {
    private XLog log;
    private XFactory factory;
    private Map<String, XTrace> traceMap = new HashMap<>();


    private static XesLogger instance;

    private XesLogger() {
        factory = XFactoryRegistry.instance().currentDefault();
        log = factory.createLog();

        // Retrieve the extensions from the XExtensionManager
        XExtension conceptExtension = XExtensionManager.instance().getByUri(XConceptExtension.EXTENSION_URI);
        XExtension organizationalExtension = XExtensionManager.instance().getByUri(XOrganizationalExtension.EXTENSION_URI);
        XExtension timeExtension = XExtensionManager.instance().getByUri(XTimeExtension.EXTENSION_URI);

        // Add extensions to the log
        log.getExtensions().add(conceptExtension);
        log.getExtensions().add(organizationalExtension);
        log.getExtensions().add(timeExtension);
    }

    public static XesLogger getInstance() {
        if (instance == null) {
            instance = new XesLogger();
        }
        return instance;
    }


    public XTrace createNewTrace(String traceId) {
        XTrace trace = factory.createTrace();
        trace.getAttributes().put("application_id", factory.createAttributeLiteral("application_id", traceId, null));
        traceMap.put(traceId, trace);
        log.add(trace);
        return trace;
    }

    public void appendEvent(String traceId, Map<String, Object> attributes) {
        XTrace trace = traceMap.get(traceId);
        if (trace == null) {
            throw new IllegalArgumentException("Trace not found: " + traceId);
        }
        XEvent event = factory.createEvent();
        attributes.forEach((key, value) -> {
            XAttribute attribute;
            if (value instanceof String) {
                attribute = factory.createAttributeLiteral(key, (String) value, null);
            } else if (value instanceof Integer) {
                attribute = factory.createAttributeDiscrete(key, (int) value, null);
            } else if (value instanceof Long) {
                attribute = factory.createAttributeDiscrete(key, (long) value, null);
            } else {
                throw new IllegalArgumentException("Unsupported attribute type: " + value.getClass());
            }
            event.getAttributes().put(key, attribute);
        });
        trace.add(event);
    }

    public void saveLog(String filename) throws IOException {
        XesXmlSerializer serializer = new XesXmlSerializer();
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            serializer.serialize(log, fos);
        }
    }

}
