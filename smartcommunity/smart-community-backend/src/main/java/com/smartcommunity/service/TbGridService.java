package com.smartcommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcommunity.entity.TbGrid;

import java.util.List;

/**
 * 网格服务接口
 */
public interface TbGridService extends IService<TbGrid> {

    /**
     * 获取网格列表（包含 GeoJSON 空间数据）
     */
    List<TbGrid> getGridListWithGeo();

    /**
     * 保存网格（处理空间数据入库）
     */
    boolean saveGrid(TbGrid grid);
}