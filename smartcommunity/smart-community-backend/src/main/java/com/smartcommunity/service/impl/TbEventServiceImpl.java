package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcommunity.entity.TbEvent;
import com.smartcommunity.entity.TbGrid;
import com.smartcommunity.mapper.TbEventMapper;
import com.smartcommunity.mapper.TbGridMapper;
import com.smartcommunity.service.TbEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TbEventServiceImpl extends ServiceImpl<TbEventMapper, TbEvent> implements TbEventService {

    @Autowired
    private TbGridMapper gridMapper;

    @Override
    public List<TbEvent> getEventListWithGeo() {
        return baseMapper.selectEventWithGeo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportEvent(TbEvent event) {
        if (event.getStatus() == null) {
            event.setStatus("pending");
        }

        // 智能分派逻辑
        if (event.getGeomJson() != null) {
            TbGrid matchedGrid = gridMapper.findGridByPoint(event.getGeomJson());
            if (matchedGrid != null) {
                event.setGridId(matchedGrid.getGridId());
            }
        }

        int rows = baseMapper.insertEvent(event);
        return rows > 0;
    }

    @Override
    public Map<String, Object> getHeatMapStats() {
        // 1. 查询原始聚合数据
        List<Map<String, Object>> rawData = baseMapper.selectEventCountByGridAndType();

        // 2. 提取 X轴 (所有网格名称，去重排序)
        List<String> xData = rawData.stream()
                .map(m -> (String) m.get("grid_name"))
                .distinct().sorted().collect(Collectors.toList());

        // 3. 提取 Y轴 (所有事件类型，去重)
        List<String> yData = rawData.stream()
                .map(m -> (String) m.get("type"))
                .distinct().collect(Collectors.toList());

        // 4. 构建 ECharts data 数组 [[xIndex, yIndex, value], ...]
        List<List<Object>> seriesData = new ArrayList<>();
        for (Map<String, Object> item : rawData) {
            String grid = (String) item.get("grid_name");
            String type = (String) item.get("type");
            Long count = (Long) item.get("count");

            int xIndex = xData.indexOf(grid);
            int yIndex = yData.indexOf(type);

            if (xIndex != -1 && yIndex != -1) {
                List<Object> point = new ArrayList<>();
                point.add(xIndex);
                point.add(yIndex);
                point.add(count);
                seriesData.add(point);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("xData", xData);
        result.put("yData", yData);
        result.put("data", seriesData);
        return result;
    }
}