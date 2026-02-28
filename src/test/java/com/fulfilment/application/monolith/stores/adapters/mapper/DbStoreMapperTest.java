package com.fulfilment.application.monolith.stores.adapters.mapper;

import com.fulfilment.application.monolith.stores.DbStore;
import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DbStoreMapperTest {

    @Inject
    private DbStoreMapper mapper;

    // Tests for toStoreDetails(DbStore dbStore)

    @Test
    void toStoreDetails_shouldMapDbStoreToStoreDetails() {
        // Given
        DbStore dbStore = new DbStore();
        dbStore.setId(1L);
        dbStore.setName("Test Store");
        dbStore.setQuantityProductsInStock(100);

        // When
        StoreDetails storeDetails = mapper.toStoreDetails(dbStore);

        // Then
        assertNotNull(storeDetails);
        assertEquals(1L, storeDetails.id());
        assertEquals("Test Store", storeDetails.name());
        assertEquals(100, storeDetails.quantityProductsInStock());
    }

    @Test
    void toStoreDetails_shouldReturnNullWhenDbStoreIsNull() {
        // When
        StoreDetails storeDetails = mapper.toStoreDetails((DbStore) null);

        // Then
        assertNull(storeDetails);
    }

    @Test
    void toStoreDetails_shouldHandleDbStoreWithNullName() {
        // Given
        DbStore dbStore = new DbStore();
        dbStore.setId(1L);
        dbStore.setName(null);
        dbStore.setQuantityProductsInStock(50);

        // When
        StoreDetails storeDetails = mapper.toStoreDetails(dbStore);

        // Then
        assertNotNull(storeDetails);
        assertEquals(1L, storeDetails.id());
        assertNull(storeDetails.name());
        assertEquals(50, storeDetails.quantityProductsInStock());
    }

    @Test
    void toStoreDetails_shouldHandleDbStoreWithZeroQuantity() {
        // Given
        DbStore dbStore = new DbStore();
        dbStore.setId(1L);
        dbStore.setName("Empty Store");
        dbStore.setQuantityProductsInStock(0);

        // When
        StoreDetails storeDetails = mapper.toStoreDetails(dbStore);

        // Then
        assertNotNull(storeDetails);
        assertEquals(0, storeDetails.quantityProductsInStock());
    }

    // Tests for toStoreDetails(List<DbStore> dbStores)

    @Test
    void toStoreDetailsList_shouldMapListOfDbStoresToListOfStoreDetails() {
        // Given
        DbStore dbStore1 = new DbStore();
        dbStore1.setId(1L);
        dbStore1.setName("Store 1");
        dbStore1.setQuantityProductsInStock(10);

        DbStore dbStore2 = new DbStore();
        dbStore2.setId(2L);
        dbStore2.setName("Store 2");
        dbStore2.setQuantityProductsInStock(20);

        List<DbStore> dbStores = Arrays.asList(dbStore1, dbStore2);

        // When
        List<StoreDetails> storeDetailsList = mapper.toStoreDetails(dbStores);

        // Then
        assertNotNull(storeDetailsList);
        assertEquals(2, storeDetailsList.size());

        assertEquals(1L, storeDetailsList.get(0).id());
        assertEquals("Store 1", storeDetailsList.get(0).name());
        assertEquals(10, storeDetailsList.get(0).quantityProductsInStock());

        assertEquals(2L, storeDetailsList.get(1).id());
        assertEquals("Store 2", storeDetailsList.get(1).name());
        assertEquals(20, storeDetailsList.get(1).quantityProductsInStock());
    }

    @Test
    void toStoreDetailsList_shouldReturnEmptyListWhenInputIsEmpty() {
        // When
        List<StoreDetails> storeDetailsList = mapper.toStoreDetails(Collections.emptyList());

        // Then
        assertNotNull(storeDetailsList);
        assertTrue(storeDetailsList.isEmpty());
    }

    @Test
    void toStoreDetailsList_shouldReturnNullWhenInputIsNull() {
        // When
        List<StoreDetails> storeDetailsList = mapper.toStoreDetails((List<DbStore>) null);

        // Then
        assertNull(storeDetailsList);
    }

    // Tests for toStoreEntity(StoreDetails storeDetails)

    @Test
    void toStoreEntity_shouldMapStoreDetailsToDbStore() {
        // Given
        StoreDetails storeDetails = new StoreDetails(1L, "Test Store", 100);

        // When
        DbStore dbStore = mapper.toStoreEntity(storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertEquals("Test Store", dbStore.getName());
        assertEquals(100, dbStore.getQuantityProductsInStock());
    }

    @Test
    void toStoreEntity_shouldReturnNullWhenStoreDetailsIsNull() {
        // When
        DbStore dbStore = mapper.toStoreEntity(null);

        // Then
        assertNull(dbStore);
    }

    @Test
    void toStoreEntity_shouldHandleStoreDetailsWithNullId() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, "New Store", 50);

        // When
        DbStore dbStore = mapper.toStoreEntity(storeDetails);

        // Then
        assertNotNull(dbStore);
        assertNull(dbStore.getId());
        assertEquals("New Store", dbStore.getName());
        assertEquals(50, dbStore.getQuantityProductsInStock());
    }

    @Test
    void toStoreEntity_shouldHandleStoreDetailsWithNullName() {
        // Given
        StoreDetails storeDetails = new StoreDetails(1L, null, 75);

        // When
        DbStore dbStore = mapper.toStoreEntity(storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertNull(dbStore.getName());
        assertEquals(75, dbStore.getQuantityProductsInStock());
    }

    // Tests for toStoreEntity(long id, StoreDetails storeDetails)

    @Test
    void toStoreEntityWithId_shouldMapWithProvidedId() {
        // Given
        StoreDetails storeDetails = new StoreDetails(999L, "Test Store", 100);
        long providedId = 5L;

        // When
        DbStore dbStore = mapper.toStoreEntity(providedId, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(providedId, dbStore.getId());
        assertEquals("Test Store", dbStore.getName());
        assertEquals(100, dbStore.getQuantityProductsInStock());
    }

    @Test
    void toStoreEntityWithId_shouldOverrideStoreDetailsId() {
        // Given
        StoreDetails storeDetails = new StoreDetails(10L, "Store", 50);
        long providedId = 20L;

        // When
        DbStore dbStore = mapper.toStoreEntity(providedId, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(20L, dbStore.getId());
    }

    // Tests for patchStoreEntity(long id, StoreDetails storeDetails)

    @Test
    void patchStoreEntity_shouldPatchWithAllValues() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, "Updated Store", 150);
        long id = 1L;

        // When
        DbStore dbStore = mapper.patchStoreEntity(id, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertEquals("Updated Store", dbStore.getName());
        assertEquals(150, dbStore.getQuantityProductsInStock());
    }

    @Test
    void patchStoreEntity_shouldNotUpdateNameWhenNull() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, null, 200);
        long id = 1L;

        // When
        DbStore dbStore = mapper.patchStoreEntity(id, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertNull(dbStore.getName());
        assertEquals(200, dbStore.getQuantityProductsInStock());
    }

    @Test
    void patchStoreEntity_shouldNotUpdateQuantityWhenZero() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, "Patched Store", 0);
        long id = 1L;

        // When
        DbStore dbStore = mapper.patchStoreEntity(id, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertEquals("Patched Store", dbStore.getName());
        assertEquals(0, dbStore.getQuantityProductsInStock());
    }

    @Test
    void patchStoreEntity_shouldSetOnlyIdWhenAllFieldsAreNullOrZero() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, null, 0);
        long id = 5L;

        // When
        DbStore dbStore = mapper.patchStoreEntity(id, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(5L, dbStore.getId());
        assertNull(dbStore.getName());
        assertEquals(0, dbStore.getQuantityProductsInStock());
    }

    @Test
    void patchStoreEntity_shouldHandleNegativeQuantity() {
        // Given
        StoreDetails storeDetails = new StoreDetails(null, "Store", -10);
        long id = 1L;

        // When
        DbStore dbStore = mapper.patchStoreEntity(id, storeDetails);

        // Then
        assertNotNull(dbStore);
        assertEquals(1L, dbStore.getId());
        assertEquals("Store", dbStore.getName());
        assertEquals(-10, dbStore.getQuantityProductsInStock());
    }
}

