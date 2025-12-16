package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcommunity.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 登录业务
     */
    SysUser login(String username, String password);

    /**
     * 获取用户列表（包含部门名称）
     */
    IPage<SysUser> selectUserList(Integer pageNum, Integer pageSize, SysUser user);

    /**
     * 校验用户名是否唯一
     */
    boolean checkUserNameUnique(String username);

    /**
     * 重置用户密码
     */
    boolean resetUserPwd(Long userId, String password);

    /**
     * 修改用户状态
     */
    boolean updateUserStatus(Long userId, String status);
}