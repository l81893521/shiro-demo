package permission;

import com.alibaba.druid.util.StringUtils;
import org.apache.shiro.authz.Permission;

import java.util.Arrays;

/**
 * Created by Will.Zhang on 2016/12/1 0001 16:04.
 */
public class BitPermission implements Permission {

    private String resourcesIdentify;
    private int permissionBit;
    private String instanceId;

    public BitPermission(String permissionString) {
        String[] array = permissionString.split("\\+");
        if(array.length > 1){
            resourcesIdentify = array[1];
        }
        if(StringUtils.isEmpty(resourcesIdentify)){
            resourcesIdentify = "*";
        }
        if(array.length > 2){
            permissionBit = Integer.valueOf(array[2]);
        }
        if(array.length > 3){
            instanceId = array[3];
        }
        if(StringUtils.isEmpty(instanceId)){
            instanceId = "*";
        }
    }

    public boolean implies(Permission permission) {
        if(!(permission instanceof  BitPermission)){
            return false;
        }
        //需要验证的权限
        BitPermission other = (BitPermission) permission;
        if(!"*".equals(this.resourcesIdentify) && !this.resourcesIdentify.equals(other.resourcesIdentify)){
            return false;
        }
        if(this.permissionBit != 0 && (this.permissionBit & other.permissionBit) == 0){
            return false;
        }
        if(!"*".equals(instanceId) && !this.instanceId.equals(other.instanceId)){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BitPermission{" +
                "resourcesIdentify='" + resourcesIdentify + '\'' +
                ", permissionBit=" + permissionBit +
                ", instanceId='" + instanceId + '\'' +
                '}';
    }
}
