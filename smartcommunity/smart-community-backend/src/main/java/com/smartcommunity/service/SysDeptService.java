package com.smartcommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcommunity.entity.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {
    /**
     * 获取部门树形列表
     */
    List<SysDept> getDeptTree();

    /**
     * 是否存在子部门
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 部门下是否存在用户
     */
    boolean checkDeptExistUser(Long deptId);
}