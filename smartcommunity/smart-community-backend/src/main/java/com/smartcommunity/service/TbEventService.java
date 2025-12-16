package com.smartcommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcommunity.entity.TbEvent;

import java.util.List;
import java.util.Map;

/**
 * 事件服务接口
 */
public interface TbEventService extends IService<TbEvent> {

    List<TbEvent> getEventListWithGeo();

    boolean reportEvent(TbEvent event);

    /**
     * 【新增】获取热力图统计数据
     * @return 包含 xData, yData, data 的 Map
     */
    Map<String, Object> getHeatMapStats();
}