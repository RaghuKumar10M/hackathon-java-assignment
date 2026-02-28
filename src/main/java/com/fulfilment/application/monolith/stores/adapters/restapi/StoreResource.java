package com.fulfilment.application.monolith.stores.adapters.restapi;


import com.fulfilment.application.monolith.stores.StoreCreatedEvent;
import com.fulfilment.application.monolith.stores.StoreUpdatedEvent;
import com.fulfilment.application.monolith.stores.adapters.carrier.StoreDetails;
import com.fulfilment.application.monolith.stores.domain.ports.StoreOperation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/store")
@RequestScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

    @Inject
    StoreOperation storeOperation;

    @Inject
    Event<StoreCreatedEvent> storeCreatedEvent;

    @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

    public List<StoreDetails> getStores() {
        return storeOperation.getAllStores();
    }

    @GET
    @Path("{id}")
    public StoreDetails getStore(Long id) {
        return storeOperation.findByStoreId(id);
    }

    @POST
    public Response create(StoreDetails storeDetails) {
        if(storeDetails.id() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        StoreDetails persistedData = storeOperation.create(storeDetails);

        storeCreatedEvent.fireAsync(new StoreCreatedEvent(persistedData));

        return Response.ok(persistedData).status(201).build();
    }

    @PUT
    @Path("{id}")
    public StoreDetails update(Long id, StoreDetails updateDetails) {
        if (updateDetails.name() == null) {
            throw new WebApplicationException("Store Name was not set on request.", 422);
        }

        StoreDetails updatedStoreDetails = storeOperation.update(id, updateDetails);

        storeUpdatedEvent.fireAsync(new StoreUpdatedEvent(updatedStoreDetails));

        return updatedStoreDetails;
    }

    @PATCH
    @Path("{id}")
    public StoreDetails patch(Long id, StoreDetails updateDetails) {
        if (updateDetails.name() == null) {
            throw new WebApplicationException("Store Name was not set on request.", 422);
        }
        StoreDetails storeDetails = storeOperation.patch(id, updateDetails);

        storeUpdatedEvent.fireAsync(new StoreUpdatedEvent(storeDetails));
        return storeDetails;
    }

    @DELETE
    @Path("{id}")
    public Response delete(Long id) {

        storeOperation.remove(id);
        return Response.status(204).build();
    }
}
