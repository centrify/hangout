package com.ctrip.ops.sysdev.decoders;

import lombok.extern.log4j.Log4j2;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class JsonDecoder implements Decode {
	private static final Logger log = Logger.getLogger(JsonDecoder.class.getName());
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
