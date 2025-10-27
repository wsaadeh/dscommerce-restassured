package com.saadeh.dscommerce.controllers;

import com.saadeh.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.apache.el.parser.Token;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {

    private String clientUsername,clientPassword,adminUsername,adminPassword;
    private String clientToken,adminToken,invalidToken;
    private Long existingProductId, nonExistingProductId,dependentId;
    private String productName;
    private Map<String, Object> postProductInstance;

    @BeforeEach
    void setUp() {

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername,clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto";

        baseURI = "http://localhost:8080";
        productName = "Macbook";
        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "batman return");
        postProductInstance.put("description", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 200.0);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories = List.of(category1, category2);

        postProductInstance.put("categories", categories);

    }

    @Test
    public void finbyIdShouldReturnProductWhenIdExist() {
        existingProductId = 2L;

        given()
                .get("/products/{id}", existingProductId)
                .then()
                .statusCode(200)
                .body("id", is(existingProductId.intValue()))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0f))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"));
    }

    @Test
    public void findAllPagedShouldReturnPagedListWhenNameIsNotFilled() {

        given()
                .get("/products")
                .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));

    }

    @Test
    public void findAllPagedShouldReturnPagedListWhenNameIsFilled() {

        given()
                .get("/products?name={productName}", productName)
                .then()
                .statusCode(200)
                .body("content[0].id", is(3))
                .body("content[0].name", equalTo("Macbook Pro"))
                .body("content[0].price", is(1250.0F))
                .body("content.imgUrl[0]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));

    }

    @Test
    public void findAllPagedShouldReturnPagedListWhenPriceIsGreaterThan2000() {

        given()
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll {it.price > 2000}.name", hasItems("PC Gamer Hera", "Smart TV", "PC Gamer Boo"));

    }

    @Test
    public void insertShouldReturnProductWhenDataIsValid() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("batman return"))
                .body("price", is(200.0F))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id",hasItems(2,3));

    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndNameIsInvalid() {
        postProductInstance.put("name","ab");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));

    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {
        postProductInstance.put("description","ab");
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));

    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsNegative() {
        postProductInstance.put("price",-50.0F);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));

    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsZero() {
        postProductInstance.put("price",0.0);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));

    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {
        postProductInstance.put("categories",null);
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));

    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLogged(){
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(403);
    }

    @Test
    public void insertShouldReturnUnAuthorizedWhenClientLogged(){
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(401);
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExistAndAdminLogged(){
        existingProductId = 25L;

        given()
                    .header("Authorization", "Bearer " + adminToken)
                 .when()
                    .delete("/products/{id}",existingProductId)
                .then()
                    .statusCode(204);
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist(){
        nonExistingProductId = 100L;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/products/{id}",nonExistingProductId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado."));
    }

    @Test
    public void deleteShouldReturnBadRequestWhenIdExistWithDependentData(){
        dependentId = 3L;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/products/{id}",dependentId)
                .then()
                .statusCode(400);
    }

    @Test
    public void deleteShouldReturnForbiddenWhenClientLogged(){
        nonExistingProductId = 100L;

        given()
                .header("Authorization", "Bearer " + clientToken)
                .when()
                .delete("/products/{id}",nonExistingProductId)
                .then()
                .statusCode(403);
    }

    @Test
    public void deleteShouldReturnUnAuthorizedWhenUserNotLoggedIdExist(){
        nonExistingProductId = 100L;

        given()
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .delete("/products/{id}",nonExistingProductId)
                .then()
                .statusCode(401);
    }




}
