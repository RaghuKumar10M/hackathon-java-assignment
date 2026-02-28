package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;

import java.util.List;

public interface StoreDbOperation {

    List<StoreDetails> getAllStores();

    StoreDetails findByStoreId(Long id);

    StoreDetails create(StoreDetails store);

    StoreDetails update(Long id, StoreDetails store);

    StoreDetails patch (Long id, StoreDetails store);

    void remove(Long id);

    void removeAll();
}
