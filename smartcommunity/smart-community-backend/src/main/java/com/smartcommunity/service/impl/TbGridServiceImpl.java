package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcommunity.entity.TbGrid;
import com.smartcommunity.mapper.TbGridMapper;
import com.smartcommunity.service.TbGridService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 网格服务实现类
 */
@Service
public class TbGridServiceImpl extends ServiceImpl<TbGridMapper, TbGrid> implements TbGridService {

    @Override
    public List<TbGrid> getGridListWithGeo() {
        // 调用 Mapper 中我们要手写的 PostGIS 查询方法
        return baseMapper.selectGridWithGeo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public boolean saveGrid(TbGrid grid) {
        // 1. 设置默认值
        if (grid.getStatus() == null) {
            grid.setStatus("normal");
        }

        // 2. 调用 Mapper 中手写的插入方法 (包含 ST_GeomFromGeoJSON 转换)
        int rows = baseMapper.insertGrid(grid);
        return rows > 0;
    }
}