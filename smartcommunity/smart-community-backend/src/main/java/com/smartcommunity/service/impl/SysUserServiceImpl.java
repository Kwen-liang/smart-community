package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcommunity.entity.SysUser;
import com.smartcommunity.mapper.SysUserMapper;
import com.smartcommunity.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user != null && user.getPassword().equals(password)) {
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public IPage<SysUser> selectUserList(Integer pageNum, Integer pageSize, SysUser user) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        // 调用 Mapper 自定义的 XML 查询方法
        return baseMapper.selectUserList(page, user);
    }

    @Override
    public boolean checkUserNameUnique(String username) {
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        return count == 0;
    }

    @Override
    public boolean resetUserPwd(Long userId, String password) {
        return this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
                .set(SysUser::getPassword, password));
    }

    @Override
    public boolean updateUserStatus(Long userId, String status) {
        return this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
                .set(SysUser::getStatus, status));
    }
}