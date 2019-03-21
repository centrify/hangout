package com.ctrip.ops.sysdev.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseMetric;
import com.ctrip.ops.sysdev.config.CommandLineValues;
import com.ctrip.ops.sysdev.config.HangoutConfig;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {
	static private final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        //Parse CommandLine arguments
        CommandLineValues cm = new CommandLineValues(args);
        cm.parseCmd();

        // parse configure file
        Map configs = null;
        try {
            configs = HangoutConfig.parse(cm.getConfigFile());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
        log.debug(configs);

        final List<HashMap<String, Map>> inputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");
        final List<HashMap<String, Map>> filterConfigs = (ArrayList<HashMap<String, Map>>) configs.get("filters");
        final List<HashMap<String, Map>> outputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("outputs");
        final List<HashMap<String, Map>> metricsConfigs = (ArrayList<HashMap<String, Map>>) configs.get("metrics");

        if (metricsConfigs != null) {
            metricsConfigs.forEach(metric -> {
                metric.forEach((metricType, metricConfig) -> {
                    log.info("begin to build metric " + metricType);

                    Class<?> metricClass = null;

                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.metrics." + metricType, metricType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            metricClass = Class.forName(className);
                            Constructor<?> ctor = metricClass.getConstructor(Map.class);
                            BaseMetric metricInstance = (BaseMetric) ctor.newInstance(metricConfig);
                            log.info("build metric " + metricType + " done");

                            metricInstance.register();
                            log.info("metric" + metricType + " started");

                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                log.info("maybe a third party metric plugin. try to build " + metricType);
                                tryCtrip = false;
                                continue;
                            } else {
                                log.error(e);
                                throw new IllegalStateException(e.getMessage());
                            }
                        } catch (Exception e) {
                            log.error(e);
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                });
            });
        }

        // for input in all_inputs, Go through every input and emit immediately
        inputConfigs.forEach(
                input -> {
                    input.forEach((inputType, inputConfig) -> {
                        log.info("begin to build input " + inputType);

                        Class<?> inputClass = null;

                        List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.inputs." + inputType, inputType);
                        boolean tryCtrip = true;
                        for (String className : classNames) {
                            try {
                                inputClass = Class.forName(className);
                                //Get Constructor for each input
                                Constructor<?> ctor = inputClass.getConstructor(
                                        Map.class,
                                        ArrayList.class,
                                        ArrayList.class);
                                //instantiate the input,prepare() and registerShutdownHookForSelf() are called here.
                                BaseInput inputInstance = (BaseInput) ctor.newInstance(
                                        inputConfig,
                                        filterConfigs,
                                        outputConfigs);

                                log.info("build input " + inputType + " done");
                                //Start working,guy.
                                inputInstance.emit();
                                log.info("input" + inputType + " started");
                                break;
                            } catch (ClassNotFoundException e) {
                                if (tryCtrip == true) {
                                    log.info("maybe a third party input plugin. try to build " + inputType);
                                    tryCtrip = false;
                                    continue;
                                } else {
                                    log.error(e);
                                    throw new IllegalStateException(e.getMessage());
                                }
                            } catch (Exception e) {
                                log.error(e);
                                throw new IllegalStateException(e.getMessage());
                            }
                        }
                    });
                });
    }
}

