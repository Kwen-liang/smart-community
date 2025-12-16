package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.entity.TbEvent;
import com.smartcommunity.service.TbEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 事件处置控制器
 */
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private TbEventService eventService;

    /**
     * 获取所有事件 (包含 GeoJSON 空间数据) - 用于 GIS 地图展示
     */
    @GetMapping("/list")
    public Result<List<TbEvent>> list() {
        return Result.success(eventService.getEventListWithGeo());
    }

    /**
     * 根据状态获取事件列表 - 用于审核台账
     * @param status pending/processing/archived
     */
    @GetMapping("/listByStatus")
    public Result<List<TbEvent>> listByStatus(@RequestParam String status) {
        // 使用 MyBatis-Plus 的条件查询
        return Result.success(eventService.list(new LambdaQueryWrapper<TbEvent>()
                .eq(TbEvent::getStatus, status)
                .orderByDesc(TbEvent::getOccurTime)));
    }

    /**
     * 上报事件
     */
    @PostMapping("/report")
    public Result<Boolean> report(@RequestBody TbEvent event) {
        // 设置初始时间
        event.setCreateTime(java.time.LocalDateTime.now());
        boolean success = eventService.reportEvent(event);
        if (success) {
            return Result.success("事件上报成功，已进入调度中心", true);
        } else {
            return Result.error("上报失败");
        }
    }

    /**
     * 任务分派 (审核通过)
     * @param eventId 事件ID
     * @param handlerId 指定的处理人ID
     */
    @PostMapping("/dispatch")
    public Result<Boolean> dispatch(@RequestParam Long eventId, @RequestParam Long handlerId) {
        boolean success = eventService.update(new LambdaUpdateWrapper<TbEvent>()
                .eq(TbEvent::getEventId, eventId)
                .set(TbEvent::getStatus, "processing")
                .set(TbEvent::getHandlerId, handlerId)
                .set(TbEvent::getUpdateTime, java.time.LocalDateTime.now()));

        return Result.success("任务分派成功", success);
    }

    /**
     * 驳回事件
     */
    @PostMapping("/reject")
    public Result<Boolean> reject(@RequestParam Long eventId, @RequestParam String reason) {
        // 简单处理：状态改为 rejected 或 archived，并记录备注
        boolean success = eventService.update(new LambdaUpdateWrapper<TbEvent>()
                .eq(TbEvent::getEventId, eventId)
                .set(TbEvent::getStatus, "rejected")
                // 实际建议增加 audit_comment 字段，这里简化暂存 description 或忽略
                .set(TbEvent::getUpdateTime, java.time.LocalDateTime.now()));
        return Result.success("事件已驳回", success);
    }

    /**
     * 结案归档
     */
    @PostMapping("/close")
    public Result<Boolean> close(@RequestParam Long eventId) {
        boolean success = eventService.update(new LambdaUpdateWrapper<TbEvent>()
                .eq(TbEvent::getEventId, eventId)
                .set(TbEvent::getStatus, "archived")
                .set(TbEvent::getUpdateTime, java.time.LocalDateTime.now()));
        return Result.success("事件已归档", success);
    }
}