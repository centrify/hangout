package com.ctrip.ops.sysdev.baseplugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BaseOutput extends Base {
	static private final Logger log = LogManager.getLogger(BaseOutput.class);
    protected Map config;
    protected List<TemplateRender> IF;

    public BaseOutput(Map config) {
        super(config);

        this.config = config;

        if (this.config.containsKey("if")) {
            IF = new ArrayList<TemplateRender>();
            for (String c : (List<String>) this.config.get("if")) {
                try {
                    IF.add(new FreeMarkerRender(c, c));
                } catch (IOException e) {
                    log.fatal(e.getMessage());
                    throw new IllegalStateException("add if error." + e.getMessage());
                }
            }
        } else {
            IF = null;
        }

        this.prepare();
    }

    protected abstract void prepare();

    protected abstract void emit(Map event);

    public void shutdown() {
        log.info("shutdown" + this.getClass().getName());
    }

    public void process(Map event) {
        boolean ifSuccess = true;
        if (this.IF != null) {
            for (TemplateRender render : this.IF) {
                if (!render.render(event).equals("true")) {
                    ifSuccess = false;
                    break;
                }
            }
        }
        if (ifSuccess) {
            this.emit(event);
            if (this.enableMeter == true) {
                this.meter.mark();
            }
        }
    }
}
