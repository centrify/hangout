package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Replace extends BaseFilter {
	static private final Logger log = LogManager.getLogger(Replace.class);
    public Replace(Map config) {
        super(config);
    }

    private TemplateRender templateRender;
    private TemplateRender srcTemplateRender;
    private FieldSetter fieldSetter;

    protected void prepare() {
        String src = (String) config.get("src");
        this.fieldSetter = FieldSetter.getFieldSetter(src);
        try {
            this.srcTemplateRender = TemplateRender.getRender(src, false);
        } catch (IOException e) {
            log.fatal("could NOT build tempalte render from " + src);
            throw new IllegalStateException("could NOT build tempalte render from " + src);
        }

        String value = (String) config.get("value");
        try {
            this.templateRender = TemplateRender.getRender(value);
        } catch (IOException e) {
            log.fatal("could NOT build tempalte render from " + value);
            throw new IllegalStateException("could NOT build tempalte render from " + value);
        }
    }

    @Override
    protected Map filter(final Map event) {
        if (this.srcTemplateRender.render(event) != null) {
            this.fieldSetter.setField(event, this.templateRender.render(event));
        }
        return event;
    }
}
