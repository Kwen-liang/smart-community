package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 自定义分页查询（关联部门表获取 deptName）
     * @param page 分页对象
     * @param user 查询条件
     * @return 分页结果
     */
    IPage<SysUser> selectUserList(Page<SysUser> page, @Param("user") SysUser user);
}