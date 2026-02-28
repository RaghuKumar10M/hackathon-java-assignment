package com.fulfilment.application.monolith.stores.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.fulfilment.application.monolith.stores.DbStore;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

    private static final String BASE_PATH = "/store";

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void setup() {
        // Clean slate - reset to known state
        em.createQuery("DELETE FROM DbStore").executeUpdate();

        // Reset the sequence first
        em.createNativeQuery("ALTER SEQUENCE store_seq RESTART WITH 1").executeUpdate();

        // Re-insert test data - let Hibernate generate the IDs
        DbStore store1 = new DbStore();
        store1.setName("TONSTAD");
        store1.setQuantityProductsInStock(10);
        em.persist(store1);

        DbStore store2 = new DbStore();
        store2.setName("KALLAX");
        store2.setQuantityProductsInStock(5);
        em.persist(store2);

        DbStore store3 = new DbStore();
        store3.setName("BESTÅ");
        store3.setQuantityProductsInStock(3);
        em.persist(store3);


        em.flush();
    }

    // ==================== GET Store By ID Tests ====================

    @Nested
    @DisplayName("GET /store/{id} - Get Store By ID")
    class GetStoreByIdTests {

        @Test
        @DisplayName("Should return store when it exists")
        void testGetStoreById_Success() {
            given()
                .when()
                .get(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
                .body("name", equalTo("TONSTAD"))
                .body("quantityProductsInStock", equalTo(10));
        }

        @Test
        @DisplayName("Should return 404 when store does not exist")
        void testGetStoreById_NotFound() {
            given()
                .when()
                .get(BASE_PATH + "/999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return another existing store")
        void testGetStoreById_AnotherStore() {
            given()
                .when()
                .get(BASE_PATH + "/2")
                .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("name", equalTo("KALLAX"))
                .body("quantityProductsInStock", equalTo(5));
        }
    }

    // ==================== POST Create Store Tests ====================

    @Nested
    @DisplayName("POST /store - Create Store")
    class CreateStoreTests {

        @Test
        @DisplayName("Should create store successfully")
        void testCreateStore_Success() {
            String requestBody = """
                {
                    "name": "NEW_STORE",
                    "quantityProductsInStock": 25
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("NEW_STORE"))
                .body("quantityProductsInStock", equalTo(25));
        }

        @Test
        @DisplayName("Should return 422 when id is provided in request")
        void testCreateStore_IdProvidedShouldFail() {
            String requestBody = """
                {
                    "id": 100,
                    "name": "INVALID_STORE",
                    "quantityProductsInStock": 10
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(422);
        }

        @Test
        @DisplayName("Should create store with zero stock")
        void testCreateStore_WithZeroStock() {
            String requestBody = """
                {
                    "name": "EMPTY_STORE",
                    "quantityProductsInStock": 0
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("name", equalTo("EMPTY_STORE"))
                .body("quantityProductsInStock", equalTo(0));
        }

        @Test
        @DisplayName("Should persist created store in database")
        void testCreateStore_PersistsInDatabase() {
            String uniqueName = "PERSIST_TEST_" + System.currentTimeMillis();
            String requestBody = """
                {
                    "name": "%s",
                    "quantityProductsInStock": 15
                }
                """.formatted(uniqueName);

            // Create the store
            int createdId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract()
                .path("id");

            // Verify it can be retrieved
            given()
                .when()
                .get(BASE_PATH + "/" + createdId)
                .then()
                .statusCode(200)
                .body("name", equalTo(uniqueName))
                .body("quantityProductsInStock", equalTo(15));
        }
    }

    // ==================== PUT Update Store Tests ====================

    @Nested
    @DisplayName("PUT /store/{id} - Update Store")
    class UpdateStoreTests {

        @Test
        @DisplayName("Should update store successfully")
        void testUpdateStore_Success() {
            String requestBody = """
                {
                    "name": "UPDATED_TONSTAD",
                    "quantityProductsInStock": 100
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("UPDATED_TONSTAD"))
                .body("quantityProductsInStock", equalTo(100));
        }

        @Test
        @DisplayName("Should return 422 when name is not provided")
        void testUpdateStore_NoNameShouldFail() {
            String requestBody = """
                {
                    "quantityProductsInStock": 50
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/1")
                .then()
                .statusCode(422);
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent store")
        void testUpdateStore_NotFound() {
            String requestBody = """
                {
                    "name": "NON_EXISTENT",
                    "quantityProductsInStock": 10
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should update store and persist changes")
        void testUpdateStore_PersistsChanges() {
            String requestBody = """
                {
                    "name": "KALLAX_UPDATED",
                    "quantityProductsInStock": 999
                }
                """;

            // Update the store
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/2")
                .then()
                .statusCode(200);

            // Verify the changes persisted
            given()
                .when()
                .get(BASE_PATH + "/2")
                .then()
                .statusCode(200)
                .body("name", equalTo("KALLAX_UPDATED"))
                .body("quantityProductsInStock", equalTo(999));
        }
    }

    // ==================== PATCH Store Tests ====================

    @Nested
    @DisplayName("PATCH /store/{id} - Patch Store")
    class PatchStoreTests {

        @Test
        @DisplayName("Should patch store successfully")
        void testPatchStore_Success() {
            String requestBody = """
                {
                    "name": "PATCHED_NAME",
                    "quantityProductsInStock": 50
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
        }

        @Test
        @DisplayName("Should return 422 when name is not provided")
        void testPatchStore_NoNameShouldFail() {
            String requestBody = """
                {
                    "quantityProductsInStock": 75
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/1")
                .then()
                .statusCode(422);
        }

        @Test
        @DisplayName("Should return 404 when patching non-existent store")
        void testPatchStore_NotFound() {
            String requestBody = """
                {
                    "name": "NON_EXISTENT_PATCH",
                    "quantityProductsInStock": 10
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch(BASE_PATH + "/999")
                .then()
                .statusCode(404);
        }
    }

    // ==================== DELETE Store Tests ====================

    @Nested
    @DisplayName("DELETE /store/{id} - Delete Store")
    class DeleteStoreTests {

        @Test
        @DisplayName("Should delete store successfully")
        void testDeleteStore_Success() {
            given()
                .when()
                .delete(BASE_PATH + "/1")
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Should not find deleted store")
        void testDeleteStore_NotFoundAfterDelete() {
            // First, delete the store
            given()
                .when()
                .delete(BASE_PATH + "/3")
                .then()
                .statusCode(204);

            // Verify it's no longer retrievable
            given()
                .when()
                .get(BASE_PATH + "/3")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent store")
        void testDeleteStore_NotFound() {
            given()
                .when()
                .delete(BASE_PATH + "/999")
                .then()
                .statusCode(404);
        }
    }

    // ==================== Content Type Tests ====================

    @Nested
    @DisplayName("Content Type Validation Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("GET should return JSON content type")
        void testGetReturnsJson() {
            given()
                .when()
                .get(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("POST should accept and return JSON")
        void testPostJsonContentType() {
            String requestBody = """
                {
                    "name": "JSON_TEST_STORE",
                    "quantityProductsInStock": 5
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("PUT should accept and return JSON")
        void testPutJsonContentType() {
            String requestBody = """
                {
                    "name": "JSON_UPDATE",
                    "quantityProductsInStock": 30
                }
                """;

            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }
    }
}

