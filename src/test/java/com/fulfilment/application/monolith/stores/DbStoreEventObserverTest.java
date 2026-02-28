package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import com.fulfilment.application.monolith.stores.adapters.database.StoreRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class DbStoreEventObserverTest {

  @Inject
  StoreEventObserver storeEventObserver;

  @InjectMock
  LegacyStoreManagerGateway legacyGateway;

  @Inject
  StoreRepository storeRepository;

  private StoreDetails testDbStore;

  @BeforeEach
  @Transactional
  void setup() {
    storeRepository.deleteAll();
    testDbStore = new StoreDetails( null,"Test Store", 10);
  }

  @Test
  void testStoreCreatedEventCallsLegacyGateway() throws InterruptedException {
    Mockito.reset(legacyGateway);

    StoreCreatedEvent event = new StoreCreatedEvent(testDbStore);
    storeEventObserver.onStoreCreated(event);
    
    Thread.sleep(100);
    
    verify(legacyGateway, times(1)).createStoreOnLegacySystem(any(StoreDetails.class));
  }

  @Test
  void testStoreUpdatedEventCallsLegacyGateway() throws InterruptedException {
    Mockito.reset(legacyGateway);

    StoreUpdatedEvent event = new StoreUpdatedEvent(testDbStore);
    storeEventObserver.onStoreUpdated(event);
    
    Thread.sleep(100);
    
    verify(legacyGateway, times(1)).updateStoreOnLegacySystem(any(StoreDetails.class));
  }
}
