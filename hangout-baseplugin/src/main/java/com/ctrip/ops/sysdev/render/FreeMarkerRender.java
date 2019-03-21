package com.ctrip.ops.sysdev.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FreeMarkerRender implements TemplateRender {
	static private final Logger log = LogManager.getLogger(FreeMarkerRender.class);
    private Template t;

    public FreeMarkerRender(String template, String templateName)
            throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        this.t = new Template(templateName, template, cfg);
    }

    public Object render(Map event) {
        StringWriter sw = new StringWriter();
        try {
            t.process(event, sw);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(event);
            return null;
        }
        try {
            sw.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return sw.toString();
    }
}
