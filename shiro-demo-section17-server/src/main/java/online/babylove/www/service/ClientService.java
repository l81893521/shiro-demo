package online.babylove.www.service;

import online.babylove.www.entity.Client;

import java.util.List;

/**
 * Created by Will.Zhang on 2017/4/17 0017 15:56.
 */
public interface ClientService {

    /**
     * 创建客户端
     * @param client
     * @return
     */
    public Client createClient(Client client);

    /**
     * 更新客户端
     * @param client
     * @return
     */
    public Client updateClient(Client client);

    /**
     * 删除客户端
     * @param clientId
     */
    public void deleteClient(Long clientId);

    /**
     * 根据clientId查找客户端
     * @param clientId
     * @return
     */
    Client findOne(Long clientId);

    /**
     * 查找所有客户端
     * @return
     */
    List<Client> findAll();

    /**
     * 根据clientId查找客户端
     * @param clientId
     * @return
     */
    Client findByClientId(String clientId);

    /**
     * 根据clientSecret查找客户端
     * @param clientSecret
     * @return
     */
    Client findByClientSecret(String clientSecret);
}
