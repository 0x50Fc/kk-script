package cn.kkmofang.script;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2018/1/25.
 */

public class ScriptContext implements IScriptContext {

    private static final ThreadLocal<Stack<IScriptContext>> _contexts = new ThreadLocal<>();

    public static void pushContext(IScriptContext context) {
        Stack<IScriptContext> q = _contexts.get();
        if(q == null) {
            q = new Stack<>();
            _contexts.set(q);
        }
        q.push(context);
    }

    public static IScriptContext currentContext() {

        Stack<IScriptContext> q = _contexts.get();

        if(q != null && !q.isEmpty()) {
            return q.peek();
        }

        return null;
    }

    public static IScriptContext popContext() {

        Stack<IScriptContext> q = _contexts.get();

        if(q != null && !q.isEmpty()) {
            return q.pop();
        }

        return null;
    }

    public static String stringValue(Object value ,String defaultValue) {

        if(value == null) {
            return defaultValue;
        }

        if(value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    public static int intValue(Object value,int defaultValue) {

        if(value == null) {
            return defaultValue;
        }

        if(value instanceof Number) {
            return ((Number) value).intValue();
        }

        if(value instanceof String) {

        }

        return defaultValue;
    }

    public static long longValue(Object value,long defaultValue) {

        if(value == null) {
            return defaultValue;
        }

        if(value instanceof Number) {
            return ((Number) value).longValue();
        }

        return defaultValue;
    }

    public static boolean booleanValue(Object value,boolean defaultValue) {

        if(value == null) {
            return defaultValue;
        }

        if(value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        if(value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }

        if(value instanceof String) {
            return ((String)value).equals("true") || ((String)value).equals("yes");
        }

        return defaultValue;
    }

    public static String[] keys(Object object) {

        if(object == null) {
            return null;
        }

        if(object instanceof Map) {

            Map v = (Map) object;
            String[] keys = new String[v.size()];
            int i = 0;

            for(Object key : v.keySet()) {
                keys[i++] = stringValue(key,"");
            }

            return keys;
        }

        if(object instanceof Collection) {

            Collection v = (Collection) object;

            String[] keys = new String[v.size()];

            for(int i=0;i<v.size();i++) {
                keys[i] = String.valueOf(i);
            }

            return keys;
        }

        if(object.getClass().isArray()) {

            int length = Array.getLength(object);

            String[] keys = new String[length];

            for(int i=0;i<length;i++) {
                keys[i] = String.valueOf(i);
            }

            return keys;

        }

        if(object instanceof IScriptObject) {
            return ((IScriptObject) object).keys();
        }

        Class<?> clazz = object.getClass();

        Set<String> keys = new TreeSet<>();

        while(clazz != null) {

            for(Field fd : clazz.getFields()) {
                ScriptProperty v = fd.getAnnotation(ScriptProperty.class);
                if(v != null) {
                    if("".equals(v.value())) {
                        keys.add(fd.getName());
                    } else {
                        keys.add(v.value());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }
        return keys.toArray(new String[keys.size()]);

    }

    public static Object get(Object object, String key) {

        if(object == null || key == null) {
            return null;
        }

        if(object instanceof Map) {
            Map v = (Map) object;
            if(v.containsKey(key)) {
                return v.get(key);
            }
            return null;
        }

        if(object instanceof List) {

            List v = (List) object;

            if("length".equals(key)) {
                return v.size();
            }

            int i = intValue(key,0);

            if(i >=0 && i < v.size()) {
                return v.get(i);
            }

            return null;

        }

        if(object.getClass().isArray()) {

            int length = Array.getLength(object);

            if("length".equals(key)) {
                return length;
            }

            int i = intValue(key,0);

            if(i >=0 && i < length) {
                return Array.get(object,i);
            }

            return null;

        }

        if(object instanceof IScriptObject) {

            return ((IScriptObject) object).get(key);

        }

        Class<?> clazz = object.getClass();

        while(clazz != null) {

            try {
                Field fd = clazz.getField(key);
                return fd.get(object);
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e) {
                return null;
            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    public static void set(Object object, String key , Object value) {

        if(object == null || key == null) {
            return ;
        }

        if(object instanceof Map) {
            Map v = (Map) object;
            v.put(key,value);
            return;
        }

        if(object instanceof List) {

            List v = (List) object;

            int i = intValue(key,0);

            if(i >=0 && i < v.size()) {
                v.set(i,value);
            } else if(i == v.size()) {
                v.add(value);
            }

            return ;
        }

        if(object.getClass().isArray()) {

            int length = Array.getLength(object);

            int i = intValue(key,0);

            if(i >=0 && i < length) {
                Array.set(object,i,value);
            }

            return ;

        }

        if(object instanceof IScriptObject) {
            ((IScriptObject) object).set(key,value);
            return;
        }

        Class<?> clazz = object.getClass();

        while(clazz != null) {

            try {
                Field fd = clazz.getField(key);
                fd.set(object,value);
                return;
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e) {
                return;
            }

            clazz = clazz.getSuperclass();
        }

    }

}
