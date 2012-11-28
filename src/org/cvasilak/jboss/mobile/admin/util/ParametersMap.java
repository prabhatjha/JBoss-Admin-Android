package org.cvasilak.jboss.mobile.admin.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParametersMap implements Map<String, Object> {

    private Map<String, Object> params = new HashMap<String, Object>();

    @Override
    public void clear() {
        params.clear();
    }

    @Override
    public boolean containsKey(Object o) {
        return params.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return params.containsValue(o);
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return params.entrySet();
    }

    @Override
    public Object get(Object o) {
        return params.get(o);
    }

    @Override
    public boolean isEmpty() {
        return params.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return params.keySet();
    }

    @Override
    public Object put(String s, Object o) {
        return params.put(s, o);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        params.putAll(map);
    }

    @Override
    public Object remove(Object o) {
        return params.remove(o);
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public Collection<Object> values() {
        return params.values();
    }

    public static ParametersMap newMap() {
        return new ParametersMap();
    }

    public ParametersMap add(String name, Object value) {
        params.put(name, value);
        return this;
    }
}
