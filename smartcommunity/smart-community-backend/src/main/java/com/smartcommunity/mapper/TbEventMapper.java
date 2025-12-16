package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcommunity.entity.TbEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TbEventMapper extends BaseMapper<TbEvent> {
    /**
     * 查询所有事件，包含 GeoJSON 数据
     */
    List<TbEvent> selectEventWithGeo();

    /**
     * 插入事件（包含空间转换）
     */
    int insertEvent(TbEvent event);

    /**
     * 【新增】统计各网格不同类型的事件数量
     * 结果示例: [{grid_name="A区", type="facilities", count=5}, ...]
     */
    @Select("SELECT g.name as grid_name, e.type, COUNT(*) as count " +
            "FROM tb_event e " +
            "LEFT JOIN tb_grid g ON e.grid_id = g.grid_id " +
            "WHERE g.name IS NOT NULL " +
            "GROUP BY g.name, e.type")
    List<Map<String, Object>> selectEventCountByGridAndType();
}