package com.olusegunlight.ledger.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ErrorResponsesTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void unknownAccountIs404ProblemDetail() {
        assertThat(mvc.get().uri("/api/v1/accounts/{id}/balance", "wallet:nobody"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void openingTheSameWalletTwiceIs409() {
        openWallet("err-test");

        assertThat(mvc.post().uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"handle": "err-test", "name": "Again"}
                        """))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("ACCOUNT_ALREADY_EXISTS");
    }

    @Test
    void transferFromAnEmptyWalletIs422() {
        openWallet("err-empty");
        openWallet("err-target");

        assertThat(mvc.post().uri("/api/v1/transactions/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fromAccountId": "wallet:err-empty", "toAccountId": "wallet:err-target", "amount": "10.00"}
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    void malformedAmountIs400() {
        openWallet("err-amount");

        assertThat(mvc.post().uri("/api/v1/accounts/{id}/deposits", "wallet:err-amount")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount": "10.005"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void reversingAnUnknownEntryIs404() {
        assertThat(mvc.post().uri("/api/v1/transactions/{id}/reversals", "no-such-entry"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("ENTRY_NOT_FOUND");
    }

    private void openWallet(String handle) {
        assertThat(mvc.post().uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"handle\": \"" + handle + "\", \"name\": \"Test\"}"))
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void transferAboveTheLimitIs422WithTheLimit() {
        openWallet("err-big-from");
        openWallet("err-big-to");

        var response = assertThat(mvc.post().uri("/api/v1/transactions/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fromAccountId": "wallet:err-big-from", "toAccountId": "wallet:err-big-to", "amount": "1000000.01"}
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson();

        response.extractingPath("$.code").isEqualTo("TRANSFER_LIMIT_EXCEEDED");
        response.extractingPath("$.limit").isEqualTo("1000000.00");
    }

    @Test
    void depositWithoutAnAmountIs400NamingTheField() {
        openWallet("val-empty");

        var response = assertThat(mvc.post().uri("/api/v1/accounts/{id}/deposits", "wallet:val-empty")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        response.extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
        response.extractingPath("$.errors[0].field").isEqualTo("amount");
        response.extractingPath("$.errors[0].message").isEqualTo("must not be null");
    }

    @Test
    void openAccountReportsEveryInvalidField() {
        var response = assertThat(mvc.post().uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"handle": "Bad Handle!", "name": ""}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        response.extractingPath("$.errors.length()").isEqualTo(2);
        response.extractingPath("$.errors[0].field").isEqualTo("handle");
        response.extractingPath("$.errors[1].field").isEqualTo("name");
    }

    @Test
    void openingAWalletReturnsItsLocation() {
        assertThat(mvc.post().uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"handle": "val-location", "name": "Located"}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/api/v1/accounts/wallet:val-location");
    }

    @Test
    void transferToTheSameWalletIs400() {
        var response = assertThat(mvc.post().uri("/api/v1/transactions/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fromAccountId": "wallet:val-self", "toAccountId": "wallet:val-self", "amount": "100.00"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        response.extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
        response.extractingPath("$.errors[0].field").isEqualTo("toAccountDifferent");
    }

    @Test
    void transferWithMissingFieldsIs400() {
        assertThat(mvc.post().uri("/api/v1/transactions/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors.length()").isEqualTo(3);
    }
}