package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcommunity.entity.TbGrid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TbGridMapper extends BaseMapper<TbGrid> {
    /**
     * 查询所有网格，包含 GeoJSON
     */
    List<TbGrid> selectGridWithGeo();

    /**
     * 插入网格
     */
    int insertGrid(TbGrid grid);

    /**
     * 【新增】核心算法：查找指定坐标点落在哪个网格内
     * @param pointJson GeoJSON 格式的点，例如 { "type": "Point", "coordinates": [111.6, 29.0] }
     * @return 匹配到的网格对象
     */
    TbGrid findGridByPoint(@Param("pointJson") String pointJson);
}