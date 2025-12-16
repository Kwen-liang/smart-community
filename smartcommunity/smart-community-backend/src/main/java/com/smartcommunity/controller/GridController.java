package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.entity.TbGrid;
import com.smartcommunity.service.TbGridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网格管理控制器
 */
@RestController
@RequestMapping("/grid")
public class GridController {

    @Autowired
    private TbGridService gridService;

    /**
     * 获取所有网格 (包含 GeoJSON 空间数据)
     * 用于前端 GIS 地图绘制
     */
    @GetMapping("/list")
    public Result<List<TbGrid>> list() {
        return Result.success(gridService.getGridListWithGeo());
    }

    /**
     * 保存绘制的网格
     * 前端传来的 GeoJSON 字符串会自动映射到实体类的 geomJson 字段
     */
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody TbGrid grid) {
        // 简单模拟属性填充 (实际业务中这些可能由前端填写)
        if (grid.getArea() == null) {
            grid.setArea(new java.math.BigDecimal("1500.00"));
        }
        if (grid.getPopulation() == null) {
            grid.setPopulation(0);
        }

        boolean success = gridService.saveGrid(grid);
        if (success) {
            return Result.success("网格保存成功", true);
        } else {
            return Result.error("保存失败");
        }
    }

    /**
     * 【新增】删除网格接口
     * 解决前端 DELETE 404 报错
     * 对应请求: DELETE /api/grid/{gridId}
     */
    @DeleteMapping("/{gridId}")
    public Result<Boolean> delete(@PathVariable Long gridId) {
        // 调用 MyBatis-Plus 提供的默认删除方法
        boolean success = gridService.removeById(gridId);
        if (success) {
            return Result.success("网格删除成功", true);
        } else {
            return Result.error("删除失败，请稍后重试");
        }
    }
}