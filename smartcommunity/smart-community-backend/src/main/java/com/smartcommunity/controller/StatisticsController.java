package com.smartcommunity.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.entity.TbEvent;
import com.smartcommunity.entity.TbGrid;
import com.smartcommunity.service.TbEventService;
import com.smartcommunity.service.TbGridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatisticsController {

    @Autowired
    private TbGridService gridService;
    @Autowired
    private TbEventService eventService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // ... (保留之前的 getCardStats, getPieData, getHeatMapData 方法) ...
    // 为了完整性，这里我只列出你需要新增/修改的部分，请将以下方法添加到类中

    @GetMapping("/cards")
    public Result<Map<String, Object>> getCardStats() {
        // ... (保持之前的代码不变) ...
        // 如果你不想自己拼接，可以直接把之前生成的内容放这里
        String cacheKey = "dashboard:stats:cards";
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            Map<Object, Object> cachedEntries = redisTemplate.opsForHash().entries(cacheKey);
            Map<String, Object> result = new HashMap<>();
            cachedEntries.forEach((k, v) -> result.put((String) k, Long.parseLong((String) v)));
            return Result.success("查询成功(Cache)", result);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("gridCount", gridService.count());
        map.put("population", gridService.list().stream().mapToLong(g -> g.getPopulation() == null ? 0 : g.getPopulation()).sum());
        map.put("todayCount", eventService.count(new QueryWrapper<TbEvent>().apply("DATE(occur_time) = DATE(NOW())")));
        map.put("pendingCount", eventService.count(new QueryWrapper<TbEvent>().eq("status", "pending")));
        Map<String, String> stringMap = new HashMap<>();
        map.forEach((k, v) -> stringMap.put(k, String.valueOf(v)));
        redisTemplate.opsForHash().putAll(cacheKey, stringMap);
        redisTemplate.expire(cacheKey, 60, TimeUnit.SECONDS);
        return Result.success(map);
    }

    @GetMapping("/pie")
    public Result<List<Map<String, Object>>> getPieData() {
        // ... (保持之前的代码不变) ...
        List<TbEvent> list = eventService.list();
        Map<String, Integer> typeCount = new HashMap<>();
        for (TbEvent e : list) {
            String typeName = translateType(e.getType());
            typeCount.put(typeName, typeCount.getOrDefault(typeName, 0) + 1);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        typeCount.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("value", v);
            result.add(item);
        });
        return Result.success(result);
    }

    @GetMapping("/heatmap")
    public Result<Map<String, Object>> getHeatMapData() {
        // ... (保持之前的代码不变) ...
        String cacheKey = "dashboard:stats:heatmap";
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            Map<String, Object> result = JSON.parseObject(cachedJson, Map.class);
            return Result.success(result);
        }
        Map<String, Object> result = eventService.getHeatMapStats();
        List<String> yData = (List<String>) result.get("yData");
        List<String> translatedY = yData.stream().map(this::translateType).collect(Collectors.toList());
        result.put("yData", translatedY);
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 5, TimeUnit.MINUTES);
        return Result.success(result);
    }

    /**
     * 【新增】柱状图数据：各网格事件数量Top5
     */
    @GetMapping("/bar")
    public Result<Map<String, Object>> getBarData() {
        // 1. 获取热力图的原始统计数据 (复用 Service 逻辑)
        Map<String, Object> rawStats = eventService.getHeatMapStats();
        List<String> xData = (List<String>) rawStats.get("xData"); // 网格名称列表
        List<List<Object>> dataPoints = (List<List<Object>>) rawStats.get("data"); // [xIndex, yIndex, value]

        // 2. 统计每个网格的总事件数
        Map<String, Integer> gridCountMap = new HashMap<>();
        for (List<Object> point : dataPoints) {
            int xIndex = (int) point.get(0);
            int value = ((Number) point.get(2)).intValue();
            String gridName = xData.get(xIndex);
            gridCountMap.put(gridName, gridCountMap.getOrDefault(gridName, 0) + value);
        }

        // 3. 排序并取前 7 个
        List<Map.Entry<String, Integer>> sortedList = gridCountMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 降序
                .limit(7)
                .collect(Collectors.toList());

        // 4. 组装前端需要的格式
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedList) {
            categories.add(entry.getKey());
            values.add(entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("data", values);

        return Result.success(result);
    }

    private String translateType(String type) {
        if ("facilities".equals(type)) return "设施维护";
        if ("sanitation".equals(type)) return "市容环境";
        if ("security".equals(type)) return "治安防控";
        if ("emergency".equals(type)) return "突发事件";
        if ("service".equals(type)) return "便民服务";
        return type == null ? "未知" : type;
    }
}