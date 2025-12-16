package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcommunity.entity.TbPoi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TbPoiMapper extends BaseMapper<TbPoi> {
    // 简单查询，直接用注解省去 XML
    @Select("SELECT id, name, type, ST_AsGeoJSON(geom) as geomJson FROM tb_poi")
    List<TbPoi> selectPoiList();
}