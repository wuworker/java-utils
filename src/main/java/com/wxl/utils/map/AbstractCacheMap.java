package com.wxl.utils.map;

import java.util.AbstractMap;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 缓存map抽象父类
 */
public abstract class AbstractCacheMap<K,V> extends AbstractMap<K,V>
        implements CacheMap<K,V>{


    /**
     * 是否是过期的时间
     */
    protected boolean isExpireTime(long expire) {
        return expire <= 0 && expire != PERSISTENT_KEY;
    }





}



