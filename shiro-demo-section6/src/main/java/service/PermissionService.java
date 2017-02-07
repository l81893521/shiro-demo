package service;

import entity.Permission;

/**
 * Created by Will.Zhang on 2017/2/7 0007 14:52.
 */
public interface PermissionService {

    /**
     * 创建权限
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
