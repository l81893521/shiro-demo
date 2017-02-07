package dao;

import entity.User;

import java.util.Set;

/**
 * Created by Will.Zhang on 2017/2/7 0007 14:48.
 */
public interface UserDao {

    /**
     * 创建用户
     * @param user
     * @return
     */
    public User createUser(User user);

    /**
     * 更新用户
     * @param user
     */
    public void updateUser(User user);

    /**
     * 删除用户
     * @param userId
     */
    public void deleteUser(Long userId);

    /**
     * 关联角色
     * @param userId
     * @param roleIds
     */
    public void correlationRoles(Long userId, Long... roleIds);

    /**
     * 解除关联角色
     * @param userId
     * @param roleIds
     */
    public void uncorrelationRoles(Long userId, Long... roleIds);

    /**
     * 查找用户
     * @param userId
     * @return
     */
    User findOne(Long userId);

    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    User findByUsername(String username);

    /**
     * 查找角色
     * @param username
     * @return
     */
    Set<String> findRoles(String username);

    /**
     * 查找权限
     * @param username
     * @return
     */
    Set<String> findPermissions(String username);
}
