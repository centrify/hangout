package com.ctrip.ops.sysdev.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.rmi.CORBA.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import lombok.extern.log4j.Log4j2;


@SuppressWarnings("ALL")
@Log4j2
public class Utils {
	static private final Logger log = LogManager.getLogger(Util.class);
    public static List<BaseFilter> createFilterProcessors(List<Map> filters) {
        List<BaseFilter> filterProcessors = new ArrayList();

        if (filters != null) {
            filters.stream().forEach((Map filterMap) -> {
                filterMap.entrySet().stream().forEach(entry -> {
                    Map.Entry<String, Map> filter = (Map.Entry<String, Map>) entry;
                    String filterType = filter.getKey();
                    Map filterConfig = filter.getValue();

                    log.info("begin to build filter " + filterType);

                    Class<?> filterClass;
                    Constructor<?> ctor = null;
                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.filters." + filterType, filterType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            filterClass = Class.forName(className);
                            ctor = filterClass.getConstructor(Map.class);
                            log.info("build filter " + filterType + " done");
                            filterProcessors.add((BaseFilter) ctor.newInstance(filterConfig));
                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                log.info("maybe a third party output plugin. try to build " + filterType);
                                tryCtrip = false;
                                continue;
                            } else {
                                log.error(e);
                                throw new IllegalStateException(e.getMessage());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                });
            });
        }

        return filterProcessors;
    }

}
