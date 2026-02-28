package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import com.fulfilment.application.monolith.stores.domain.ports.StoreDbOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@ApplicationScoped
public class StoreUserCase implements StoreOperation {

    @Inject
    StoreDbOperation storeDbOperation;

    @Override
    public List<StoreDetails> getAllStores() {
        return List.of();
    }

    @Override
    public StoreDetails findByStoreId(long id) {
        StoreDetails storeDetails = storeDbOperation.findByStoreId(id);
        if (storeDetails == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        return storeDetails;
    }

    @Override
    @Transactional
    public StoreDetails create(StoreDetails store) {
        return storeDbOperation.create(store);

    }

    @Override
    @Transactional
    public StoreDetails update(long id, StoreDetails updateDetails) {
        StoreDetails storeDetails = storeDbOperation.findByStoreId(id);
        if (storeDetails == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        return storeDbOperation.update(id, updateDetails);
    }

    @Override
    @Transactional
    public StoreDetails patch(long id, StoreDetails store) {
        StoreDetails storeDetails = storeDbOperation.findByStoreId(id);
        if (storeDetails == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        return storeDbOperation.patch(id, store);
    }

    @Override
    @Transactional
    public void remove(long id) {
        StoreDetails storeDetails = storeDbOperation.findByStoreId(id);
        if (storeDetails == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        storeDbOperation.remove(id);
    }
}
