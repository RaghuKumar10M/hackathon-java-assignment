package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;

import java.util.List;

public interface StoreOperation {
    List<StoreDetails> getAllStores();

    StoreDetails findByStoreId(long id);

    StoreDetails create(StoreDetails store);

    StoreDetails update(long id, StoreDetails store);

    StoreDetails patch (long id, StoreDetails store);

    void remove(long id);
}
