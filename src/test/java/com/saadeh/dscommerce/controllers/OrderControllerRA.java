package com.saadeh.dscommerce.controllers;

import com.saadeh.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderControllerRA {

    private String clientUsername,clientPassword,adminUsername,adminPassword;
    private String clientToken,adminToken,invalidToken;
    private Long existingOrderId, nonExistingOrderId,dependentOrderId;

    @BeforeEach
    void setUp(){
        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername,clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto";

        baseURI = "http://localhost:8080";
    }

    @Test
    public void findByIdShouldReturnOrderWhenAdminLoggedAndIdExist(){
        existingOrderId = 1L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(existingOrderId.intValue()))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.id", is(1))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment",equalTo("2022-07-25T15:00:00Z"))
                .body("items.name", hasItems("The Lord of the Rings","Macbook Pro"));
    }

    @Test
    public void findByIdShouldReturnOrderWhenClientLoggedAndOrderBelongToHim(){
        existingOrderId = 1L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(existingOrderId.intValue()))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.id", is(1))
                .body("client.name", equalTo("Maria Brown"))
                .body("total", is(1431.0F));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenOrderNotBelongToClient(){
        existingOrderId = 2L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenOrderNotExistAndAdminLogged(){
        nonExistingOrderId = 100L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenOrderNotExistAndClientLogged(){
        nonExistingOrderId = 100L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturnUnAuthorizedWhenNotLoggedWithAdminOrClient(){
        existingOrderId = 2L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(401);
    }


}
