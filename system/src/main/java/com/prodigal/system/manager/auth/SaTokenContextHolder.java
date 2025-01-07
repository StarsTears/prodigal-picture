package com.prodigal.system.manager.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 基于ThreadLocal实现上下文管理
 **/
public class SaTokenContextHolder {
    private static final ThreadLocal<Map<String,Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);
    //设置上下文
    public static void setContext(String key, Object value) {
        CONTEXT.get().put(key,value);
    }
    public static Object getContext(String key) {
        return CONTEXT.get().get(key);
    }
    //清理上下文数据，防止内存泄露
   public static void clear() {
        CONTEXT.remove();
   }
}
