package dao;

import entity.Permission;

/**
 * Created by Will.Zhang on 2017/1/11 0011 15:42.
 */
public interface PermissionDao {

    /**
     * 添加权限
     * @param permission
     * @return
     */
    public Permission createPermission(Permission permission);

    /**
     * 删除权限
     * @param permissionId
     */
    public void deletePermission(Long permissionId);
}
