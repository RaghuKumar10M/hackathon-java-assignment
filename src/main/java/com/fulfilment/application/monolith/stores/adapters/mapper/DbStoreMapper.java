package com.fulfilment.application.monolith.stores.adapters.mapper;

import com.fulfilment.application.monolith.common.MapStructConfig;
import com.fulfilment.application.monolith.stores.DbStore;
import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(config = MapStructConfig.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DbStoreMapper {

    List<StoreDetails> toStoreDetails(List<DbStore> dbStores);

    StoreDetails toStoreDetails(DbStore dbStore);

    DbStore toStoreEntity(StoreDetails storeDetails);

    @Mapping(target = "id", source = "id")
    DbStore toStoreEntity(long id, StoreDetails storeDetails);

    default DbStore patchStoreEntity(long id, StoreDetails storeDetails) {
        DbStore dbStore = new DbStore();
        dbStore.setId(id);
        if(storeDetails.name() != null) {
            dbStore.setName(storeDetails.name());
        }

        if(storeDetails.quantityProductsInStock() != 0) {
            dbStore.setQuantityProductsInStock(storeDetails.quantityProductsInStock());
        }
        return dbStore;
    }
}
