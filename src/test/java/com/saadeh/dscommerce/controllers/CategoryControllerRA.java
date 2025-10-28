package com.saadeh.dscommerce.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CategoryControllerRA {

    @BeforeEach
    void setUp() {
        baseURI = "http://localhost:8080";
    }

    @Test
    public void findAllShouldReturnListOfCategories(){
        given()
                .get("/categories")
                .then()
                .statusCode(200)
                .body("id",hasItems(1,2,3))
                .body("name", hasItems("Livros","Eletrônicos","Computadores") );
    }
}
