package com.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KV extends BaseFilter {
	static private final Logger log = LogManager.getLogger(KV.class);
    private String source;
    private String target;
    private String field_split;
    private String value_split;
    private String field_encloser;
    private String value_encloser;
    
    private String trim;
    private String trimkey;
    
    private String patternString = "([\\w-]+)=(((\")([^\"]*?)(\\4))|([^\\s]+))"; // default pattern
    private Pattern pattern; 

    private ArrayList<String> excludeKeys, includeKeys;

    @SuppressWarnings("rawtypes")
    public KV(Map config) {
        super(config);
    }

    @SuppressWarnings("unchecked")
    protected void prepare() {

        boolean bUseDefPattern = true;
        
        if (this.config.containsKey("source")) {
            this.source = (String) this.config.get("source");
        } else {
            this.source = "message";
        }

        if (this.config.containsKey("target")) {
            this.target = (String) this.config.get("target");
        }

        if (this.config.containsKey("field_split")) {
            this.field_split = (String) this.config.get("field_split");
            bUseDefPattern = false;
        } else {
            this.field_split = " ";
        }
        
        if (this.config.containsKey("value_split")) {
            this.value_split = (String) this.config.get("value_split");
            bUseDefPattern = false;
        } else {
            this.value_split = "=";
        }

        if (this.config.containsKey("trim")) {
            this.trim = (String) this.config.get("trim");
            this.trim = "^[" + this.trim + "]+|[" + this.trim + "]+$";
        }

        if (this.config.containsKey("trimkey")) {
            this.trimkey = (String) this.config.get("trimkey");
            this.trimkey = "^[" + this.trimkey + "]+|[" + this.trimkey + "]+$";
        }

        if (this.config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) this.config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "KVfail";
        }

        this.excludeKeys = (ArrayList<String>) this.config.get("exclude_keys");
        this.includeKeys = (ArrayList<String>) this.config.get("include_keys");
        
        /* Added by LD, 2019/4/8 */
        if (this.config.containsKey("field_encloser")) {
            this.field_encloser = (String) this.config.get("field_encloser");
            bUseDefPattern = false;
        } else {
            this.field_encloser = null;
        }
        
        if (this.config.containsKey("value_encloser")) {
            this.value_encloser = (String) this.config.get("value_encloser");
            bUseDefPattern = false;
        } else {
            this.value_encloser = "\"";
        }
        
        /* make RE pattern */
        // "\"?([\\w-]+)\"?=(((\")([^\"]*?)(\\4))|([^\\s]+))"; // default pattern
        
        /* field */
        if( !bUseDefPattern ) {
            StringBuilder sbPattern = new StringBuilder();
            if( field_encloser != null )
                sbPattern.append(field_encloser).append("?");
            sbPattern.append("([\\w-]+)");
            if( field_encloser != null )
                sbPattern.append(field_encloser).append("?");
            
            /* value_split */
            if( !value_split.equals(" ") )
                sbPattern.append(value_split);
            else
                sbPattern.append("\\s");
            
            /* value */
            // (((\\")([^\"]*?)(\\4))
            sbPattern.append("(((").append(value_encloser).append(")").append("([^").append(value_encloser).append("]*?)(\\4))");
            
            /* field_split */
            sbPattern.append("|([^");
            
            if( field_split.equals(" ") )
                sbPattern.append("\\s");
            else
                sbPattern.append(field_split);
            
            sbPattern.append("]+))");
            
            /* pattern */
            this.patternString = sbPattern.toString();            
        }
        
        this.pattern = Pattern.compile(patternString);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Map filter(Map event) {
        if (!event.containsKey(this.source)) {
            return event;
        }
        boolean success = true;

        HashMap targetObj = new HashMap();

        /*
        try {
            String sourceStr = (String) event.get(this.source);
            for (String kv : sourceStr.split(this.field_split)) {
                String[] kandv = kv.split(this.value_split, 2);
                if (kandv.length != 2) {
                    success = false;
                    continue;
                }

                String k = kandv[0];
                if (this.includeKeys != null && !this.includeKeys.contains(k)
                        || this.excludeKeys != null
                        && this.excludeKeys.contains(k)) {
                    continue;
                }

                String v = kandv[1];

                if (this.trim != null) {
                    v = v.replaceAll(this.trim, "");
                }
                if (this.trimkey != null) {
                    k = k.replaceAll(this.trimkey, "");
                }

                if (this.target != null) {
                    targetObj.put(k, v);
                } else {
                    event.put(k, v);
                }
            }

            if (this.target != null) {
                event.put(this.target, targetObj);
            }
        } catch (Exception e) {
            log.warn(event + "kv faild");
            success = false;
        }
        */
        try {
            String sourceStr = (String) event.get(this.source);            
            Matcher matcher = pattern.matcher(sourceStr);

            while (matcher.find()) {
                String k = matcher.group(1);
                String v = null;
                if (matcher.group(5) != null) {
                    v = matcher.group(5);
                } else {
                    v = matcher.group(2);
                }
                
                if (this.includeKeys != null && !this.includeKeys.contains(k)
                        || this.excludeKeys != null
                        && this.excludeKeys.contains(k)) {
                    continue;
                }

                if (this.trim != null) {
                    v = v.replaceAll(this.trim, "");
                }
                if (this.trimkey != null) {
                    k = k.replaceAll(this.trimkey, "");
                }

                if (this.target != null) {
                    targetObj.put(k, v);
                } else {
                    event.put(k, v);
                }
            }
            
            if (this.target != null) {
                event.put(this.target, targetObj);
            }
        } catch (Exception e) {
            log.warn(event + "kv faild");
            success = false;            
        }        

        this.postProcess(event, success);

        return event;
    }
}
