package permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;

/**
 * Created by Will.Zhang on 2016/12/1 0001 16:41.
 */
public class BitAndWildPermissionResolver implements PermissionResolver{
    public Permission resolvePermission(String permissionString) {
        if(permissionString.startsWith("+")){
            return new BitPermission(permissionString);
        }
        return new WildcardPermission(permissionString);
    }
}
