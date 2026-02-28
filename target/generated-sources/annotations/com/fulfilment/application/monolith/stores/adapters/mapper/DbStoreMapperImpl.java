package com.fulfilment.application.monolith.stores.adapters.mapper;

import com.fulfilment.application.monolith.stores.DbStore;
import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-28T16:11:44+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Ubuntu)"
)
@ApplicationScoped
public class DbStoreMapperImpl implements DbStoreMapper {

    @Override
    public List<StoreDetails> toStoreDetails(List<DbStore> dbStores) {
        if ( dbStores == null ) {
            return null;
        }

        List<StoreDetails> list = new ArrayList<StoreDetails>( dbStores.size() );
        for ( DbStore dbStore : dbStores ) {
            list.add( toStoreDetails( dbStore ) );
        }

        return list;
    }

    @Override
    public StoreDetails toStoreDetails(DbStore dbStore) {
        if ( dbStore == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        int quantityProductsInStock = 0;

        if ( dbStore.getId() != null ) {
            id = dbStore.getId();
        }
        if ( dbStore.getName() != null ) {
            name = dbStore.getName();
        }
        quantityProductsInStock = dbStore.getQuantityProductsInStock();

        StoreDetails storeDetails = new StoreDetails( id, name, quantityProductsInStock );

        return storeDetails;
    }

    @Override
    public DbStore toStoreEntity(StoreDetails storeDetails) {
        if ( storeDetails == null ) {
            return null;
        }

        DbStore dbStore = new DbStore();

        if ( storeDetails.id() != null ) {
            dbStore.setId( storeDetails.id() );
        }
        if ( storeDetails.name() != null ) {
            dbStore.setName( storeDetails.name() );
        }
        dbStore.setQuantityProductsInStock( storeDetails.quantityProductsInStock() );

        return dbStore;
    }

    @Override
    public DbStore toStoreEntity(long id, StoreDetails storeDetails) {
        if ( storeDetails == null ) {
            return null;
        }

        DbStore dbStore = new DbStore();

        if ( storeDetails != null ) {
            if ( storeDetails.name() != null ) {
                dbStore.setName( storeDetails.name() );
            }
            dbStore.setQuantityProductsInStock( storeDetails.quantityProductsInStock() );
        }
        dbStore.setId( id );

        return dbStore;
    }
}
