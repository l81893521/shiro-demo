package online.babylove.www.dao;

import online.babylove.www.entity.User;

import java.util.List;

/**
 * Created by Will.Zhang on 2017/4/17 0017 15:06.
 */
public interface UserDao {

    /**
     * 创建
     * @param user
     * @return
     */
    User createUser(User user);

    /**
     * 更新
     * @param user
     * @return
     */
    User updateUser(User user);

    /**
     * 删除
     * @param userId
     */
    void deleteUser(Long userId);

    /**
     * 根据id查找
     * @param userId
     * @return
     */
    User findOne(Long userId);

    /**
     * 查找所有
     * @return
     */
    List<User> findAll();

    /**
     * 根据用户名查找
     * @param username
     * @return
     */
    User findByUsername(String username);
}
