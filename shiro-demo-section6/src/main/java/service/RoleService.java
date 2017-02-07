package service;

import entity.Role;

/**
 * Created by Will.Zhang on 2017/2/7 0007 14:57.
 */
public interface RoleService {

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
     * 添加角色-权限之间关系
     * @param roleId
     * @param permissionIds
     */
    public void correlationPermissions(Long roleId, Long... permissionIds);

    /**
     * 移除角色-权限之间关系
     * @param roleId
     * @param permissionIds
     */
    public void uncorrelationPermissions(Long roleId, Long... permissionIds);
}
