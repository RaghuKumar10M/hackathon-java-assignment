package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreCreatedEvent {
  private final StoreDetails storeDetails;
}
