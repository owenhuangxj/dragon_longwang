package com.trenska.longwang.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.PageHelper;

public class PageUtils {
    /**
     * 获取分页参数
     * @param json
     * @return
     */
    public static Page getPageParam(JSONObject json) {
        int current = json.getIntValue("current");
        int size = json.getIntValue("size");
        if (current == 0) current = 1;
        if (size == 0) size = 10;
        return new Page(current, size);
    }
    public static Page getPageParam(PageHelper pageHelper) {
        return new Page(pageHelper.getCurrent(), pageHelper.getSize());
}
}
