package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcommunity.common.Result;
import com.smartcommunity.entity.SysUser;
import com.smartcommunity.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器 (CRUD)
 */
@RestController
@RequestMapping("/system/user")
public class UserController {

    @Autowired
    private SysUserService userService;

    /**
     * 分页查询用户列表 (关联部门名称)
     */
    @GetMapping("/list")
    public Result<IPage<SysUser>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       SysUser user) {
        IPage<SysUser> page = userService.selectUserList(pageNum, pageSize, user);
        return Result.success(page);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody SysUser user) {
        if (!userService.checkUserNameUnique(user.getUsername())) {
            return Result.error("新增用户'" + user.getUsername() + "'失败，登录账号已存在");
        }
        user.setCreateTime(java.time.LocalDateTime.now());
        // 默认密码 123456
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword("123456");
        }
        return Result.success(userService.save(user));
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Boolean> edit(@RequestBody SysUser user) {
        return Result.success(userService.updateById(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Boolean> remove(@PathVariable Long userId) {
        // 实际生产中建议做逻辑删除
        return Result.success(userService.removeById(userId));
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public Result<Boolean> resetPwd(@RequestBody SysUser user) {
        return Result.success(userService.resetUserPwd(user.getUserId(), user.getPassword()));
    }

    /**
     * 状态修改
     */
    @PutMapping("/changeStatus")
    public Result<Boolean> changeStatus(@RequestBody SysUser user) {
        return Result.success(userService.updateUserStatus(user.getUserId(), user.getStatus()));
    }
}