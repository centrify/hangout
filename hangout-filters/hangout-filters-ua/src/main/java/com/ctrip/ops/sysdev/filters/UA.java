package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import lombok.extern.log4j.Log4j2;
import ua_parser.Client;
import ua_parser.Parser;

@Log4j2
public class UA extends BaseFilter {
	static private final Logger log = LogManager.getLogger(UA.class);

	public UA(Map config) {
		super(config);
	}

	private String source;
	private Parser uaParser;

	protected void prepare() {
		if (!config.containsKey("source")) {
			log.error("no field configured in Json");
			throw new IllegalStateException("no field configured in Json");
		}
		this.source = (String) config.get("source");

		try {
			this.uaParser = new Parser();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected Map filter(final Map event) {
		if (event.containsKey(this.source)) {
			Client c = uaParser.parse((String) event.get(this.source));

			event.put("userAgent_family", c.userAgent.family);
			event.put("userAgent_major", c.userAgent.major);
			event.put("userAgent_minor", c.userAgent.minor);
			event.put("os_family", c.os.family);
			event.put("os_major", c.os.major);
			event.put("os_minor", c.os.minor);
			event.put("device_family", c.device.family);
		}
		return event;
	}
}
