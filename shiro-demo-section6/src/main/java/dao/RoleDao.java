package dao;

import entity.Role;

/**
 * Created by Will.Zhang on 2017/2/7 0007 14:42.
 */
public interface RoleDao {

    /**
     * 创建角色
     * @param role
     * @return
     */
    public Role createRole(Role role);

    /**
     * 删除角色
     * @param roleId
     */
    public void deleteRole(Long roleId);

    /**
     * 关联权限
     * @param roleId
     * @param permissionIds
     */
    public void correlationPermissions(Long roleId, Long... permissionIds);

    /**
     * 解除关联权限
     * @param roleId
     * @param permissionIds
     */
    public void uncorrelationPermissions(Long roleId, Long... permissionIds);
}
