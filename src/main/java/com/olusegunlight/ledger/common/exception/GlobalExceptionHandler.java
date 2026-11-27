package com.olusegunlight.ledger.common.exception;

import com.olusegunlight.ledger.common.CurrencyMismatchException;
import com.olusegunlight.ledger.domain.AccountAlreadyExistsException;
import com.olusegunlight.ledger.domain.AccountNotFoundException;
import com.olusegunlight.ledger.domain.DuplicateEntryException;
import com.olusegunlight.ledger.domain.EntryNotFoundException;
import com.olusegunlight.ledger.domain.InsufficientFundsException;
import com.olusegunlight.ledger.transaction.service.TransferLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Account not found", ex.getMessage(), "ACCOUNT_NOT_FOUND");
    }

    @ExceptionHandler(EntryNotFoundException.class)
    ProblemDetail handleEntryNotFound(EntryNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Journal entry not found", ex.getMessage(), "ENTRY_NOT_FOUND");
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    ProblemDetail handleAlreadyExists(AccountAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Account already exists", ex.getMessage(), "ACCOUNT_ALREADY_EXISTS");
    }

    @ExceptionHandler(DuplicateEntryException.class)
    ProblemDetail handleDuplicateEntry(DuplicateEntryException ex) {
        return problem(HttpStatus.CONFLICT, "Duplicate journal entry", ex.getMessage(), "DUPLICATE_ENTRY");
    }

    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient funds", ex.getMessage(), "INSUFFICIENT_FUNDS");
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    ProblemDetail handleCurrencyMismatch(CurrencyMismatchException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Currency mismatch", ex.getMessage(), "CURRENCY_MISMATCH");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage(), "INVALID_REQUEST");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Something went wrong on our side. Please try again later.", "INTERNAL_ERROR");
    }

    @ExceptionHandler(TransferLimitExceededException.class)
    ProblemDetail handleTransferLimitExceeded(TransferLimitExceededException ex) {
        ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Transfer limit exceeded",
                ex.getMessage(), "TRANSFER_LIMIT_EXCEEDED");
        problem.setProperty("limit", ex.getLimit().toMajor().toPlainString());
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}
