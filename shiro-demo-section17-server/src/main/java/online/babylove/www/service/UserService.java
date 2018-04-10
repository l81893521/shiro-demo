package online.babylove.www.service;

import online.babylove.www.entity.User;

import java.util.List;

/**
 * Created by Will.Zhang on 2017/4/17 0017 15:42.
 */
public interface UserService {

    /**
     * 创建用户
     * @param user
     */
    User createUser(User user);

    /**
     * 修改用户
     * @param user
     * @return
     */
    User updateUser(User user);

    /**
     * 删除用户
     * @param userId
     */
    void deleteUser(Long userId);

    /**
     * 修改密码
     * @param userId
     * @param newPassword
     */
    void changePassword(Long userId, String newPassword);

    /**
     * 根据id查找用户
     * @param userId
     * @return
     */
    User findOne(Long userId);

    /**
     * 查找所有用户
     * @return
     */
    List<User> findAll();

    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    public User findByUsername(String username);
}
