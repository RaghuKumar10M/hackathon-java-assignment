//package com.fulfilment.application.monolith.stores;
//
//import io.quarkus.panache.common.Sort;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.event.Event;
//import jakarta.inject.Inject;
//import jakarta.transaction.Transactional;
//import jakarta.ws.rs.Consumes;
//import jakarta.ws.rs.DELETE;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.PATCH;
//import jakarta.ws.rs.POST;
//import jakarta.ws.rs.PUT;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.WebApplicationException;
//import jakarta.ws.rs.core.Response;
//
//import java.util.List;
//
//@Path("store")
//@ApplicationScoped
//@Produces("application/json")
//@Consumes("application/json")
//public class StoreResource {
//
//  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;
//
//  @Inject Event<StoreCreatedEvent> storeCreatedEvent;
//
//  @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;
//
//  @GET
//  public List<DbStore> get() {
//    return DbStore.listAll(Sort.by("name"));
//  }
//
//  @GET
//  @Path("{id}")
//  public DbStore getSingle(Long id) {
//    DbStore entity = DbStore.findById(id);
//    if (entity == null) {
//      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
//    }
//    return entity;
//  }
//
//  @POST
//  @Transactional
//  public Response create(DbStore dbStore) {
//    if (dbStore.id != null) {
//      throw new WebApplicationException("Id was invalidly set on request.", 422);
//    }
//
//    dbStore.persist();
//    storeCreatedEvent.fireAsync(new StoreCreatedEvent(dbStore));
//
//    return Response.ok(dbStore).status(201).build();
//  }
//
//  @PUT
//  @Path("{id}")
//  @Transactional
//  public DbStore update(Long id, DbStore updatedDbStore) {
//    if (updatedDbStore.name == null) {
//      throw new WebApplicationException("Store Name was not set on request.", 422);
//    }
//
//    DbStore entity = DbStore.findById(id);
//
//    if (entity == null) {
//      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
//    }
//
//    entity.name = updatedDbStore.name;
//    entity.quantityProductsInStock = updatedDbStore.quantityProductsInStock;
//
//    storeUpdatedEvent.fireAsync(new StoreUpdatedEvent(entity));
//
//    return entity;
//  }
//
//  @PATCH
//  @Path("{id}")
//  @Transactional
//  public DbStore patch(Long id, DbStore updatedDbStore) {
//    if (updatedDbStore.name == null) {
//      throw new WebApplicationException("Store Name was not set on request.", 422);
//    }
//
//    DbStore entity = DbStore.findById(id);
//
//    if (entity == null) {
//      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
//    }
//
//    if (entity.name != null) {
//      entity.name = updatedDbStore.name;
//    }
//
//    if (entity.quantityProductsInStock != 0) {
//      entity.quantityProductsInStock = updatedDbStore.quantityProductsInStock;
//    }
//
//    storeUpdatedEvent.fireAsync(new StoreUpdatedEvent(entity));
//
//    return entity;
//  }
//
//  @DELETE
//  @Path("{id}")
//  @Transactional
//  public Response delete(Long id) {
//    DbStore entity = DbStore.findById(id);
//    if (entity == null) {
//      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
//    }
//    entity.delete();
//    return Response.status(204).build();
//  }
//}
