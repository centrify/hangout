package com.ctrip.ops.sysdev.decoders;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONValue;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JsonDecoder implements Decode {
	static private final Logger log = LogManager.getLogger(JsonDecoder.class);
    public Map<String, Object> decode(final String message) {
        Map<String, Object> event = null;
        try {
            event = (HashMap) JSONValue.parseWithException(message);
        } catch (Exception e) {
            log.debug("failed to json parse message to event", e);
        } finally {
            if (event == null) {
                event = createDefaultEvent(message);
            }
            return event;
        }
    }
}
