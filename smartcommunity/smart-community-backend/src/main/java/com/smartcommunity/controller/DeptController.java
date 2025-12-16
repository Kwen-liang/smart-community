package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.entity.SysDept;
import com.smartcommunity.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/system/dept")
public class DeptController {

    @Autowired
    private SysDeptService deptService;

    /**
     * 获取部门树形列表
     */
    @GetMapping("/list")
    public Result<List<SysDept>> list(SysDept dept) {
        return Result.success(deptService.getDeptTree());
    }

    /**
     * 新增部门
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L); // 默认为根节点
        }
        dept.setCreateTime(java.time.LocalDateTime.now());
        return Result.success(deptService.save(dept));
    }

    /**
     * 修改部门
     */
    @PutMapping
    public Result<Boolean> edit(@RequestBody SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId().equals(dept.getDeptId())) {
            return Result.error("修改部门失败，上级部门不能是自己");
        }
        return Result.success(deptService.updateById(dept));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public Result<Boolean> remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return Result.error("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return Result.error("部门存在用户,不允许删除");
        }
        return Result.success(deptService.removeById(deptId));
    }
}