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
                .extractingPath("$.code").isEqualTo("INVALID_REQUEST");
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
}