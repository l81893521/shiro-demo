package service;

import dao.PermissionDao;
import dao.PermissionDaoImpl;
import entity.Permission;

/**
 * Created by Will.Zhang on 2017/2/7 0007 14:56.
 */
public class PermissionServiceImpl implements PermissionService {

    private PermissionDao permissionDao = new PermissionDaoImpl();

    public Permission createPermission(Permission permission) {
        return permissionDao.createPermission(permission);
    }

    public void deletePermission(Long permissionId) {
        permissionDao.deletePermission(permissionId);
    }
}
