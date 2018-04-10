package online.babylove.www.dao;

import online.babylove.www.entity.Client;

import java.util.List;

/**
 * Created by Will.Zhang on 2017/4/17 0017 15:15.
 */
public interface ClientDao {

    /**
     * 创建
     * @param client
     * @return
     */
    Client createClient(Client client);

    /**
     * 更新
     * @param client
     * @return
     */
    Client updateClient(Client client);

    /**
     * 删除
     * @param clientId
     */
    void deleteClient(Long clientId);

    /**
     * 根据id查找
     * @param clientId
     * @return
     */
    Client findOne(Long clientId);

    /**
     * 查找所有
     * @return
     */
    List<Client> findAll();

    /**
     * 根据clientId查找
     * @param clientId
     * @return
     */
    Client findByClientId(String clientId);

    /**
     * 更具secret查找
     * @param clientSecret
     * @return
     */
    Client findByClientSecret(String clientSecret);
}
