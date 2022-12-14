package com.bright.card.common;

import com.bright.card.repo.entity.User;

/**
 * 用户公共信息上下文
 */
public class UserContextHolder {

    private static ThreadLocal<User> context = new InheritableThreadLocal<>();

    /**
     * 获取用户
     *
     * @return 用户信息
     */
    public static User get() {
        return context.get();
    }

    /**
     * 设置用户信息
     *
     * @param user
     */
    public static void set(User user) {
        context.set(user);
    }

    /**
     * 清空用户信息
     */
    public static void clear() {
        context.remove();
    }
}
