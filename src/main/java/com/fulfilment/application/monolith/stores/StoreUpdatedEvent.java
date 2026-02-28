package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreUpdatedEvent {
  private final StoreDetails storeDetails;
}
