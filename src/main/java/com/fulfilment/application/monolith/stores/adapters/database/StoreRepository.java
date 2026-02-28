package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.DbStore;
import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import com.fulfilment.application.monolith.stores.adapters.mapper.DbStoreMapper;
import com.fulfilment.application.monolith.stores.domain.ports.StoreDbOperation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class StoreRepository implements StoreDbOperation, PanacheRepository<DbStore> {

    @Inject
    DbStoreMapper dbStoreMapper;

    @Override
    public List<StoreDetails> getAllStores() {
        List<DbStore> stores = this.listAll();
        return dbStoreMapper.toStoreDetails(stores);
    }

    @Override
    public StoreDetails findByStoreId(Long id) {
        DbStore dbStore = this.findById(id);
        return dbStoreMapper.toStoreDetails(dbStore);
    }

    @Override
    public StoreDetails create(StoreDetails storeDetails) {
        DbStore dbStore = dbStoreMapper.toStoreEntity(storeDetails);
        this.persist(dbStore);
        return dbStoreMapper.toStoreDetails(dbStore);
    }

    @Override
    public StoreDetails update(Long id, StoreDetails storeDetails) {
        DbStore dbStore = dbStoreMapper.toStoreEntity(id, storeDetails);
        this.persist(dbStore);
        return dbStoreMapper.toStoreDetails(dbStore);
    }

    @Override
    public StoreDetails patch(Long id, StoreDetails storeDetails) {
        DbStore dbStore = dbStoreMapper.toStoreEntity(id, storeDetails);
        this.persist(dbStore);
        return dbStoreMapper.toStoreDetails(dbStore);
    }

    @Override
    public void remove(Long id) {
        this.deleteById(id);
    }

    @Override
    public void removeAll() {
        this.deleteAll();
    }

}
