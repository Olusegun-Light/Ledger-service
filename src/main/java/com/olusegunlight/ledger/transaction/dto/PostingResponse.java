package com.olusegunlight.ledger.transaction.dto;

import com.olusegunlight.ledger.domain.Posting;

public record PostingResponse(String accountId, String direction, String amount) {

    public static PostingResponse from(Posting posting) {
        return new PostingResponse(
                posting.accountId(),
                posting.direction().name(),
                posting.amount().toMajor().toPlainString()
        );
    }
}
