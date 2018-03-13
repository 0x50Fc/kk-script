package cn.kkmofang.script;

/**
 * Created by zhanghailong on 2018/1/25.
 */

public interface IScriptObject {

    public String[] keys();

    public Object get(String key);

    public void set(String key, Object value);

}
