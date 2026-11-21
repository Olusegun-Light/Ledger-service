package com.olusegunlight.ledger.transaction.dto;

public record TransferRequest(String fromAccountId, String toAccountId, String amount){
}
