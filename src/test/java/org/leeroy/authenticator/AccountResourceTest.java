package org.leeroy.authenticator;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leeroy.ResourceLoader;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.resource.AccountResource;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.BlockedAccessService;
import org.leeroy.authenticator.service.impl.AccountService;
import org.mockito.Mockito;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(AccountResource.class)
public class AccountResourceTest {

    @InjectMock
    BlockedAccessService blockedAccessService;

    @Inject
    AccountRepository accountRepository;

    @BeforeEach
    public void clearAccounts() {
        accountRepository.deleteAll().subscribeAsCompletionStage();
    }

    @Test
    public void testCreateAccountPasswordNoDigitOrSpecial() {
        given().body(ResourceLoader.load("create-account/create_account_password_invalid_no_digit_or_special.json"))
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordNoLowerCase() {
        given().body(ResourceLoader.load("create-account/create_account_password_invalid_no_lower_case.json"))
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordNoUpperCase() {
        given().body(ResourceLoader.load("create-account/create_account_password_invalid_no_upper_case.json"))
                .when().post("create-account")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCreateAccountPasswordDigit() {
        given().body(ResourceLoader.load("create-account/create_account_password_valid_digit.json"))
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordSpecial() {
        given().body(ResourceLoader.load("create-account/create_account_password_valid_special.json"))
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordLong() {
        given().body(ResourceLoader.load("create-account/create_account_password_valid_long.json"))
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCreateAccountPasswordNone() {
        given().body(ResourceLoader.load("create-account/create_account_password_valid_none.json"))
                .when().post("create-account")
                .then()
                .statusCode(200);
    }

    @Test
    public void authenticate() {
        Mockito.when(blockedAccessService.isBlocked("127.0.0.1", "android"))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .contentType(ContentType.JSON)
                .body(AuthenticateRequest.builder()
                        .ipAddress("127.0.0.1")
                        .device("android").build())
                .when().post("authenticate")
                .then()
                .statusCode(204);
    }

}