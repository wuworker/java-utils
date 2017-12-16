package com.wxl.utils;

import java.util.Random;

/**
 * Created by wuxingle on 2017/12/11.
 * 随机对象生成接口
 */
public interface RandomGenerator<T> {

    T generate(Random random);

}
