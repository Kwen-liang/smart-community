package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcommunity.entity.SysDept;
import com.smartcommunity.entity.SysUser;
import com.smartcommunity.mapper.SysDeptMapper;
import com.smartcommunity.mapper.SysUserMapper;
import com.smartcommunity.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<SysDept> getDeptTree() {
        // 1. 查询所有部门
        List<SysDept> allDepts = this.list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getOrderNum));

        // 2. 构建树形结构
        return buildTree(allDepts, 0L);
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        long count = this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId));
        return count > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId));
        return count != null && count > 0;
    }

    /**
     * 递归构建树
     */
    private List<SysDept> buildTree(List<SysDept> depts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            // 注意：这里要做空指针保护
            if (dept.getParentId() != null && dept.getParentId().equals(parentId)) {
                dept.setChildren(buildTree(depts, dept.getDeptId()));
                tree.add(dept);
            }
        }
        return tree;
    }
}