package online.babylove.www.service;

import online.babylove.www.dao.ClientDao;
import online.babylove.www.entity.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by Will.Zhang on 2017/4/17 0017 15:58.
 */
@Transactional
@Service
public class ClientServiceImpl implements ClientService{

    @Autowired
    private ClientDao clientDao;

    @Override
    public Client createClient(Client client) {
        client.setClientId(UUID.randomUUID().toString());
        client.setClientSecret(UUID.randomUUID().toString());
        return clientDao.createClient(client);
    }

    @Override
    public Client updateClient(Client client) {
        return clientDao.updateClient(client);
    }

    @Override
    public void deleteClient(Long clientId) {
        clientDao.deleteClient(clientId);
    }

    @Override
    public Client findOne(Long clientId) {
        return clientDao.findOne(clientId);
    }

    @Override
    public List<Client> findAll() {
        return clientDao.findAll();
    }

    @Override
    public Client findByClientId(String clientId) {
        return clientDao.findByClientId(clientId);
    }

    @Override
    public Client findByClientSecret(String clientSecret) {
        return clientDao.findByClientSecret(clientSecret);
    }
}
