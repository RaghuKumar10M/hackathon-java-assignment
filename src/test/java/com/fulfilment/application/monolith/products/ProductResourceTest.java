package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("ProductResource API Tests")
 class ProductResourceTest {

  private static final String BASE_PATH = "product";

  // ===================== GET ALL PRODUCTS TESTS =====================

  @Test
  @DisplayName("GET /product - Should retrieve all products sorted by name")
   void testGetAllProducts() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", is(notNullValue()));
  }

  @Test
  @DisplayName("GET /product - Should return products in JSON array format")
   void testGetAllProductsReturnsList() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(greaterThanOrEqualTo(3)));
  }

  @Test
  @DisplayName("GET /product - Products should contain expected fields")
   void testGetAllProductsHaveRequiredFields() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body(
            "[0].id", notNullValue(),
            "[0].name", notNullValue(),
            "[0].stock", notNullValue());
  }

  // ===================== GET SINGLE PRODUCT TESTS =====================

  @Test
  @DisplayName("GET /product/{id} - Should retrieve a product by valid id")
   void testGetProductById() {
    // Create a product first to ensure we have a valid id
    Product product = new Product("TEST_GET_" + System.currentTimeMillis());
    product.setStock(10);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("id", equalTo(productId), "name", equalTo(product.getName()), "stock", equalTo(10));
  }

  @Test
  @DisplayName("GET /product/{id} - Should return product with all fields populated")
   void testGetProductByIdHasAllFields() {
    // Create a product first
    Product product = new Product("TEST_FIELDS_" + System.currentTimeMillis());
    product.setDescription("Test Description");

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body(
            "id", notNullValue(),
            "name", notNullValue(),
            "stock", notNullValue());
  }

  @Test
  @DisplayName("GET /product/{id} - Should return correct product for different ids")
   void testGetProductByIdMultipleProducts() {
    // Create two products
    Product product1 = new Product("MULTI_TEST_1_" + System.currentTimeMillis());
    product1.setStock(5);

    Integer id1 =
        given()
            .contentType(ContentType.JSON)
            .body(product1)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    Product product2 = new Product("MULTI_TEST_2_" + System.currentTimeMillis());
    product2.setStock(15);

    Integer id2 =
        given()
            .contentType(ContentType.JSON)
            .body(product2)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Verify both can be retrieved correctly
    given()
        .when()
        .get(BASE_PATH + "/" + id1)
        .then()
        .statusCode(200)
        .body("name", equalTo(product1.getName()), "stock", equalTo(5));

    given()
        .when()
        .get(BASE_PATH + "/" + id2)
        .then()
        .statusCode(200)
        .body("name", equalTo(product2.getName()), "stock", equalTo(15));
  }

  @Test
  @DisplayName("GET /product/{id} - Should return 404 for non-existent product")
   void testGetProductByIdNotFound() {
    given()
        .when()
        .get(BASE_PATH + "/999")
        .then()
        .statusCode(404)
        .contentType(ContentType.JSON)
        .body(
            "code", equalTo(404),
            "error", containsString("does not exist"));
  }

  @Test
  @DisplayName("GET /product/{id} - Should return 404 with proper error message")
   void testGetProductByIdNotFoundErrorMessage() {
    given()
        .when()
        .get(BASE_PATH + "/9999")
        .then()
        .statusCode(404)
        .body(
            "exceptionType",
            equalTo("jakarta.ws.rs.WebApplicationException"),
            "error",
            containsString("Product with id of 9999 does not exist"));
  }

  @Test
  @DisplayName("GET /product/{id} - Should handle negative ids")
   void testGetProductByNegativeId() {
    given()
        .when()
        .get(BASE_PATH + "/-1")
        .then()
        .statusCode(404)
        .body("code", equalTo(404));
  }

  // ===================== CREATE PRODUCT TESTS =====================

  @Test
  @DisplayName("POST /product - Should create a new product successfully")
   void testCreateProductSuccess() {
    Product product = new Product("NEW_PRODUCT_" + System.currentTimeMillis());
    product.setDescription("Test Product Description");
    product.setPrice(new BigDecimal("99.99"));
    product.setStock(100);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body(
            "id", notNullValue(),
            "name", equalTo(product.getName()),
            "description", equalTo("Test Product Description"),
            "price", equalTo(99.99F),
            "stock", equalTo(100));
  }

  @Test
  @DisplayName("POST /product - Should create product with minimal fields")
   void testCreateProductMinimalFields() {
    Product product = new Product("MINIMAL_PRODUCT_" + System.currentTimeMillis());

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body(
            "id", notNullValue(),
            "name", equalTo(product.getName()));
  }

  @Test
  @DisplayName("POST /product - Should reject product with pre-set id")
   void testCreateProductWithIdFails() {
    Product product = new Product("INVALID_PRODUCT_" + System.currentTimeMillis());
    product.setId(999L);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(422)
        .contentType(ContentType.JSON)
        .body(
            "code", equalTo(422),
            "error", containsString("Id was invalidly set on request"));
  }

  @Test
  @DisplayName("POST /product - Should reject product with pre-set id with proper error message")
   void testCreateProductWithIdErrorMessage() {
    Product product = new Product("TEST_PRODUCT_" + System.currentTimeMillis());
    product.setId(100L);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(422)
        .body(
            "exceptionType",
            equalTo("jakarta.ws.rs.WebApplicationException"),
            "error",
            equalTo("Id was invalidly set on request."));
  }

  @Test
  @DisplayName("POST /product - Should create product and retrieve it afterwards")
   void testCreateProductAndRetrieve() {
    Product product = new Product("CREATED_PRODUCT_" + System.currentTimeMillis());
    product.setStock(50);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo(product.getName()), "stock", equalTo(50));
  }

  // ===================== UPDATE PRODUCT TESTS =====================

  @Test
  @DisplayName("PUT /product/{id} - Should update an existing product")
   void testUpdateProductSuccess() {
    // Create a product first
    Product product = new Product("ORIGINAL_FOR_UPDATE_" + System.currentTimeMillis());
    product.setStock(10);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Now update it
    Product updateProduct = new Product("UPDATED_PRODUCT_" + System.currentTimeMillis());
    updateProduct.setDescription("Updated Description");
    updateProduct.setPrice(new BigDecimal("199.99"));
    updateProduct.setStock(25);

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(
            "id", equalTo(productId),
            "name", equalTo(updateProduct.getName()),
            "description", equalTo("Updated Description"),
            "price", equalTo(199.99F),
            "stock", equalTo(25));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should update only product name")
   void testUpdateProductNameOnly() {
    // Create a product first
    Product product = new Product("NAME_UPDATE_ORIGINAL_" + System.currentTimeMillis());
    product.setStock(20);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Update only name
    Product updateProduct = new Product("NAME_CHANGED_" + System.currentTimeMillis());

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId), "name", equalTo(updateProduct.getName()));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should update all product fields")
   void testUpdateProductAllFields() {
    // Create a product first
    Product product = new Product("FULL_UPDATE_ORIGINAL_" + System.currentTimeMillis());
    product.setStock(5);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Update all fields
    Product updateProduct = new Product("FULLY_UPDATED_" + System.currentTimeMillis());
    updateProduct.setDescription("Complete Update");
    updateProduct.setPrice(new BigDecimal("299.99"));
    updateProduct.setStock(99);

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body(
            "name", equalTo(updateProduct.getName()),
            "description", equalTo("Complete Update"),
            "price", equalTo(299.99F),
            "stock", equalTo(99));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should reject update without product name")
   void testUpdateProductWithoutName() {
    // Create a product first
    Product product = new Product("UPDATE_NO_NAME_ORIGINAL_" + System.currentTimeMillis());
    product.setStock(10);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Try to update without name
    Product updateProduct = new Product();
    updateProduct.setName(null);
    updateProduct.setDescription("No Name Product");

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(422)
        .contentType(ContentType.JSON)
        .body(
            "code", equalTo(422),
            "error", containsString("Product Name was not set on request"));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should return 404 when updating non-existent product")
   void testUpdateProductNotFound() {
    Product updateProduct = new Product("NONEXISTENT_UPDATE_" + System.currentTimeMillis());

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/999")
        .then()
        .statusCode(404)
        .contentType(ContentType.JSON)
        .body(
            "code", equalTo(404),
            "error", containsString("does not exist"));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should persist updates to database")
   void testUpdateProductPersists() {
    // Create a product first
    Product product = new Product("PERSIST_ORIGINAL_" + System.currentTimeMillis());
    product.setStock(10);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Update it
    Product updateProduct = new Product("PERSISTED_UPDATE_" + System.currentTimeMillis());
    updateProduct.setStock(500);

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200);

    // Verify the update persisted
    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo(updateProduct.getName()), "stock", equalTo(500));
  }

  @Test
  @DisplayName("PUT /product/{id} - Should handle price updates correctly")
   void testUpdateProductPrice() {
    // Create a product first
    Product product = new Product("PRICE_ORIGINAL_" + System.currentTimeMillis());
    product.setPrice(new BigDecimal("50.00"));

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Update price
    Product updateProduct = new Product("PRICE_UPDATE_" + System.currentTimeMillis());
    updateProduct.setPrice(new BigDecimal("1234.56"));

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("price", equalTo(1234.56F));
  }

  @Test
  @DisplayName("DELETE /product/{id} - Should return 404 when deleting non-existent product")
   void testDeleteProductNotFound() {
    given()
        .when()
        .delete(BASE_PATH + "/9999")
        .then()
        .statusCode(404)
        .contentType(ContentType.JSON)
        .body(
            "code", equalTo(404),
            "error", containsString("does not exist"));
  }


  // ===================== CONTENT TYPE TESTS =====================

  @Test
  @DisplayName("GET /product - Should always return JSON content type")
   void testGetAllProductsContentType() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  @Test
  @DisplayName("GET /product/{id} - Should always return JSON content type")
   void testGetSingleProductContentType() {
    // Create a product first
    Product product = new Product("CONTENT_SINGLE_" + System.currentTimeMillis());

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  @Test
  @DisplayName("POST /product - Should consume and produce JSON")
   void testCreateProductContentType() {
    Product product = new Product("CONTENT_TYPE_TEST_" + System.currentTimeMillis());

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON);
  }

  @Test
  @DisplayName("PUT /product/{id} - Should consume and produce JSON")
   void testUpdateProductContentType() {
    // Create a product first
    Product product = new Product("CONTENT_UPDATE_ORIGINAL_" + System.currentTimeMillis());

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Update it
    Product updateProduct = new Product("UPDATE_CONTENT_TYPE_" + System.currentTimeMillis());

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  // ===================== ERROR HANDLING TESTS =====================

  @Test
  @DisplayName("Error Response - Should include exception type in error response")
   void testErrorResponseIncludesExceptionType() {
    given()
        .when()
        .get(BASE_PATH + "/999")
        .then()
        .statusCode(404)
        .body("exceptionType", notNullValue());
  }

  @Test
  @DisplayName("Error Response - Should include error message in error response")
   void testErrorResponseIncludesMessage() {
    given()
        .when()
        .get(BASE_PATH + "/999")
        .then()
        .statusCode(404)
        .body("error", notNullValue());
  }

  @Test
  @DisplayName("Error Response - Should include status code in error response")
   void testErrorResponseIncludesStatusCode() {
    given()
        .when()
        .get(BASE_PATH + "/999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404));
  }

  // ===================== EDGE CASE TESTS =====================

  @Test
  @DisplayName("Edge Case - Should handle product with null description")
   void testProductWithNullDescription() {
    Product product = new Product("NULL_DESC_PRODUCT_" + System.currentTimeMillis());
    product.setDescription(null);
    product.setStock(10);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("name", equalTo(product.getName()));
  }

  @Test
  @DisplayName("Edge Case - Should handle product with zero stock")
   void testProductWithZeroStock() {
    Product product = new Product("ZERO_STOCK_" + System.currentTimeMillis());
    product.setStock(0);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("stock", equalTo(0));
  }

  @Test
  @DisplayName("Edge Case - Should handle very large stock value")
   void testProductWithLargeStock() {
    Product product = new Product("LARGE_STOCK_" + System.currentTimeMillis());
    product.setStock(999999);

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("stock", equalTo(999999));
  }

  @Test
  @DisplayName("Edge Case - Should handle product with large decimal price")
   void testProductWithLargePrice() {
    Product product = new Product("LARGE_PRICE_" + System.currentTimeMillis());
    product.setPrice(new BigDecimal("9999999.99"));

    given()
        .contentType(ContentType.JSON)
        .body(product)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("price", equalTo(9999999.99F));
  }

  @Test
  @DisplayName("Integration - Create, Read, Update, Delete workflow")
   void testFullCrudWorkflow() {
    // Create
    String uniqueName = "CRUD_TEST_" + System.currentTimeMillis();
    Product product = new Product(uniqueName);
    product.setDescription("CRUD Test Product");
    product.setPrice(new BigDecimal("50.00"));
    product.setStock(25);

    Integer productId =
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // Read
    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo(uniqueName));

    // Update
    String updatedName = "CRUD_TEST_UPDATED_" + System.currentTimeMillis();
    Product updateProduct = new Product(updatedName);
    updateProduct.setStock(50);

    given()
        .contentType(ContentType.JSON)
        .body(updateProduct)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(50));

    // Delete
    given()
        .when()
        .delete(BASE_PATH + "/" + productId)
        .then()
        .statusCode(204);

    // Verify deletion
    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(404);
  }
}

