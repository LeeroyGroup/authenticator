package org.leeroy.authenticator.resource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leeroy.ResourceLoader;
import org.leeroy.authenticator.repository.AccountRepository;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(AuthenticatorResource.class)
public class AuthenticatorResourceTest {

    @Inject
    AccountRepository accountRepository;

    @BeforeEach
    public void clearAccounts() {
        accountRepository.deleteAll().subscribeAsCompletionStage();
    }

    @Test
    public void testCreateAccountPasswordNoDigitOrSpecial() {
        given().body(ResourceLoader.load("create-account/password_invalid_no_digit_or_special.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordNoLowerCase() {
        given().body(ResourceLoader.load("create-account/password_invalid_no_lower_case.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordNoUpperCase() {
        given().body(ResourceLoader.loadJson("create-account/password_invalid_no_upper_case.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordDigit() {
        given().body(ResourceLoader.load("create-account/password_valid_digit.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordSpecial() {
        given().body(ResourceLoader.load("create-account/password_valid_special.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordLong() {
        given().body(ResourceLoader.load("create-account/password_valid_long.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordNone() {
        given().body(ResourceLoader.load("create-account/password_valid_none.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testForgotPassword() {
        // First create account
        given().body(ResourceLoader.load("forgot-password/create_account.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);

        given().body(ResourceLoader.load("forgot-password/password_valid.json"))
                .contentType(ContentType.JSON)
                .when().put("forgot-password")
                .then()
                .statusCode(200);

    }

    @Test
    public void testChangePassword() {
        // First create account
        given().body(ResourceLoader.load("change-password/create_account.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);

        given().body(ResourceLoader.load("change-password/password_valid.json"))
                .contentType(ContentType.JSON)
                .when().put("change-password")
                .then()
                .statusCode(200);
    }

    @Test
    public void testDeletePassword() {
        // First create account
        given().body(ResourceLoader.load("delete-account/create_account.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);

        given().body(ResourceLoader.load("delete-account/password_valid.json"))
                .contentType(ContentType.JSON)
                .when().post("delete-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void authenticate() {
        // First create account
        given().body(ResourceLoader.load("authenticate/create_account.json"))
                .contentType(ContentType.JSON)
                .when().post("create-account")
                .then()
                .statusCode(200);

        given().body(ResourceLoader.load("authenticate/password_valid.json"))
                .contentType(ContentType.JSON)
                .when().post("authenticate-account")
                .then()
                .statusCode(200);
    }

}