package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@QuarkusTest
class WarehouseResourceTest {

  private static final String BASE_PATH = "/warehouse";

  @Inject
  EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean slate - reset to known state
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

    // Re-insert test data
    DbWarehouse wh1 = new DbWarehouse();
    wh1.setBusinessUnitCode("MWH.001");
    wh1.setLocation("ZWOLLE-001");
    wh1.setCapacity(100);
    wh1.setStock(10);
    wh1.setCreatedAt(LocalDateTime.of(2024, 7, 1, 0, 0));
    wh1.setVersion(0L);
    em.persist(wh1);

    DbWarehouse wh2 = new DbWarehouse();
    wh2.setBusinessUnitCode("MWH.012");
    wh2.setLocation("AMSTERDAM-001");
    wh2.setCapacity(50);
    wh2.setStock(5);
    wh2.setCreatedAt(LocalDateTime.of(2023, 7, 1, 0, 0));
    wh2.setVersion(0L);
    em.persist(wh2);

    DbWarehouse wh3 = new DbWarehouse();
    wh3.setBusinessUnitCode("MWH.023");
    wh3.setLocation("TILBURG-001");
    wh3.setCapacity(30);
    wh3.setStock(27);
    wh3.setCreatedAt(LocalDateTime.of(2021, 2, 1, 0, 0));
    wh3.setVersion(0L);
    em.persist(wh3);

    em.flush();
  }

  // ==================== List All Warehouses Tests ====================

  @Nested
  @DisplayName("GET /warehouse - List All Warehouses")
  class ListAllWarehousesTests {

    @Test
    @DisplayName("Should return all warehouses with status 200")
    void testListAllWarehouses_Success() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
    }

    @Test
    @DisplayName("Should return warehouses with location information")
    void testListAllWarehouses_ContainsLocationInfo() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body(
              containsString("ZWOLLE-001"),
              containsString("AMSTERDAM-001"),
              containsString("TILBURG-001"));
    }

    @Test
    @DisplayName("Should return JSON content type")
    void testListAllWarehouses_ReturnsJsonContentType() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should return at least 3 warehouses")
    void testListAllWarehouses_ReturnsAtLeastThreeItems() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("size()", greaterThanOrEqualTo(3));
    }
  }

  // ==================== Get Warehouse By ID Tests ====================

  @Nested
  @DisplayName("GET /warehouse/{id} - Get Warehouse By ID")
  class GetWarehouseByIdTests {

    @Test
    @DisplayName("Should return warehouse when it exists")
    void testGetWarehouseById_Success() {
      given()
          .when()
          .get(BASE_PATH + "/MWH.001")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.001"))
          .body("location", equalTo("ZWOLLE-001"))
          .body("capacity", equalTo(100))
          .body("stock", equalTo(10));
    }

    @Test
    @DisplayName("Should return 404 when warehouse does not exist")
    void testGetWarehouseById_NotFound() {
      given()
          .when()
          .get(BASE_PATH + "/NON-EXISTENT-ID")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return correct warehouse data for MWH.012")
    void testGetWarehouseById_ReturnCorrectData() {
      given()
          .when()
          .get(BASE_PATH + "/MWH.012")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.012"))
          .body("location", equalTo("AMSTERDAM-001"))
          .body("capacity", equalTo(50))
          .body("stock", equalTo(5));
    }
  }

  // ==================== Create Warehouse Tests ====================

  @Nested
  @DisplayName("POST /warehouse - Create Warehouse")
  class CreateWarehouseTests {

    @Test
    @DisplayName("Should create warehouse successfully with valid data")
    void testCreateWarehouse_Success() {
      String requestBody = """
          {
            "businessUnitCode": "MWH.NEW.001",
            "location": "AMSTERDAM-001",
            "capacity": 50,
            "stock": 10
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.NEW.001"))
          .body("location", equalTo("AMSTERDAM-001"))
          .body("capacity", equalTo(50))
          .body("stock", equalTo(10));

      // Verify the warehouse exists
      given()
          .when()
          .get(BASE_PATH + "/MWH.NEW.001")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.NEW.001"));
    }

    @Test
    @DisplayName("Should create warehouse with zero stock when stock is not provided")
    void testCreateWarehouse_DefaultStockToZero() {
      String requestBody = """
          {
            "businessUnitCode": "MWH.ZERO.STOCK",
            "location": "AMSTERDAM-001",
            "capacity": 25
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.ZERO.STOCK"))
          .body("stock", equalTo(0));
    }

    @Test
    @DisplayName("Should return 400 when creating warehouse with invalid location")
    void testCreateWarehouse_InvalidLocation() {
      String requestBody = """
          {
            "businessUnitCode": "MWH.INVALID",
            "location": "INVALID-LOCATION",
            "capacity": 50,
            "stock": 10
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when stock exceeds capacity")
    void testCreateWarehouse_StockExceedsCapacity() {
      String requestBody = """
          {
            "businessUnitCode": "MWH.OVER.CAPACITY",
            "location": "AMSTERDAM-001",
            "capacity": 10,
            "stock": 100
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when creating duplicate warehouse")
    void testCreateWarehouse_DuplicateBusinessUnitCode() {
      String requestBody = """
          {
            "businessUnitCode": "MWH.001",
            "location": "AMSTERDAM-001",
            "capacity": 50,
            "stock": 10
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }
  }

  // ==================== Archive Warehouse Tests ====================

  @Nested
  @DisplayName("DELETE /warehouse/{id} - Archive Warehouse")
  class ArchiveWarehouseTests {

    @Test
    @DisplayName("Should archive warehouse successfully")
    void testArchiveWarehouse_Success() {
      // Archive MWH.023
      given()
          .when()
          .delete(BASE_PATH + "/MWH.023")
          .then()
          .statusCode(204);

      // Verify the warehouse can no longer be archived again (indicates it was archived)
      given()
          .when()
          .delete(BASE_PATH + "/MWH.023")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 404 when archiving non-existent warehouse")
    void testArchiveWarehouse_NotFound() {
      given()
          .when()
          .delete(BASE_PATH + "/NON-EXISTENT-ID")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when archiving already archived warehouse")
    void testArchiveWarehouse_AlreadyArchived() {
      // First archive
      given()
          .when()
          .delete(BASE_PATH + "/MWH.012")
          .then()
          .statusCode(204);

      // Try to archive again - should fail
      given()
          .when()
          .delete(BASE_PATH + "/MWH.012")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should still be able to get archived warehouse by ID")
    void testArchiveWarehouse_CanStillRetrieveById() {
      // Archive warehouse
      given()
          .when()
          .delete(BASE_PATH + "/MWH.001")
          .then()
          .statusCode(204);

      // Verify archived warehouse can still be retrieved by ID
      given()
          .when()
          .get(BASE_PATH + "/MWH.001")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.001"))
          .body("location", equalTo("ZWOLLE-001"));
    }
  }

  // ==================== Replace Warehouse Tests ====================

  @Nested
  @DisplayName("POST /warehouse/{businessUnitCode}/replacement - Replace Warehouse")
  class ReplaceWarehouseTests {

    @Test
    @DisplayName("Should replace warehouse successfully")
    void testReplaceWarehouse_Success() {
      String requestBody = """
          {
            "location": "AMSTERDAM-001",
            "capacity": 40,
            "stock": 15
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.001/replacement")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.001"))
          .body("location", equalTo("AMSTERDAM-001"))
          .body("capacity", equalTo(40))
          .body("stock", equalTo(15));

      // Verify the warehouse was updated
      given()
          .when()
          .get(BASE_PATH + "/MWH.001")
          .then()
          .statusCode(200)
          .body("location", equalTo("AMSTERDAM-001"))
          .body("capacity", equalTo(40));
    }

    @Test
    @DisplayName("Should return 400 when replacing non-existent warehouse")
    void testReplaceWarehouse_NotFound() {
      String requestBody = """
          {
            "location": "AMSTERDAM-001",
            "capacity": 40,
            "stock": 15
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/NON-EXISTENT/replacement")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when replacing with invalid location")
    void testReplaceWarehouse_InvalidLocation() {
      String requestBody = """
          {
            "location": "INVALID-LOCATION",
            "capacity": 40,
            "stock": 15
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.001/replacement")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when stock exceeds capacity in replacement")
    void testReplaceWarehouse_StockExceedsCapacity() {
      String requestBody = """
          {
            "location": "AMSTERDAM-001",
            "capacity": 10,
            "stock": 50
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.001/replacement")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when replacing archived warehouse")
    void testReplaceWarehouse_ArchivedWarehouse() {
      // First archive the warehouse
      given()
          .when()
          .delete(BASE_PATH + "/MWH.023")
          .then()
          .statusCode(204);

      String requestBody = """
          {
            "location": "AMSTERDAM-001",
            "capacity": 40,
            "stock": 15
          }
          """;

      // Try to replace archived warehouse - should fail
      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.023/replacement")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should replace warehouse with zero stock when not provided")
    void testReplaceWarehouse_DefaultStockToZero() {
      String requestBody = """
          {
            "location": "TILBURG-001",
            "capacity": 25
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.012/replacement")
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo("MWH.012"))
          .body("stock", equalTo(0));
    }
  }

  // ==================== Integration/Edge Case Tests ====================

  @Nested
  @DisplayName("Integration and Edge Case Tests")
  class IntegrationAndEdgeCaseTests {

    @Test
    @DisplayName("Should handle full CRUD lifecycle")
    void testFullCrudLifecycle() {
      String businessUnitCode = "MWH.LIFECYCLE";

      // Create
      String createBody = """
          {
            "businessUnitCode": "MWH.LIFECYCLE",
            "location": "AMSTERDAM-001",
            "capacity": 100,
            "stock": 20
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(createBody)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(200);

      // Read
      given()
          .when()
          .get(BASE_PATH + "/" + businessUnitCode)
          .then()
          .statusCode(200)
          .body("businessUnitCode", equalTo(businessUnitCode));

      // Update (Replace)
      String updateBody = """
          {
            "location": "TILBURG-001",
            "capacity": 25,
            "stock": 10
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(updateBody)
          .when()
          .post(BASE_PATH + "/" + businessUnitCode + "/replacement")
          .then()
          .statusCode(200)
          .body("location", equalTo("TILBURG-001"));

      // Delete (Archive)
      given()
          .when()
          .delete(BASE_PATH + "/" + businessUnitCode)
          .then()
          .statusCode(204);

      // Verify archived - cannot archive again
      given()
          .when()
          .delete(BASE_PATH + "/" + businessUnitCode)
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should handle archive and verify cannot replace archived warehouse")
    void testArchiveBlocksReplacement() {
      // Archive first warehouse
      given()
          .when()
          .delete(BASE_PATH + "/MWH.001")
          .then()
          .statusCode(204);

      // Verify cannot replace archived warehouse
      String requestBody = """
          {
            "location": "AMSTERDAM-001",
            "capacity": 40,
            "stock": 15
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post(BASE_PATH + "/MWH.001/replacement")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should handle multiple create operations")
    void testMultipleCreateOperations() {
      // Create first warehouse
      String requestBody1 = """
          {
            "businessUnitCode": "MWH.CREATE.001",
            "location": "AMSTERDAM-001",
            "capacity": 50,
            "stock": 10
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody1)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(200);

      // Create second warehouse
      String requestBody2 = """
          {
            "businessUnitCode": "MWH.CREATE.002",
            "location": "TILBURG-001",
            "capacity": 25,
            "stock": 5
          }
          """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody2)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(200);

      // Verify both warehouses exist
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body(containsString("MWH.CREATE.001"), containsString("MWH.CREATE.002"));
    }
  }
}
