package com.olusegunlight.ledger.transaction.dto;

import com.olusegunlight.ledger.domain.JournalEntry;

import java.util.List;

public record EntryResponse(String id, String description, String currency, List<PostingResponse> postings) {

    public static EntryResponse from(JournalEntry entry) {
        List<PostingResponse> postings = entry.postings().stream().map(PostingResponse::from).toList();
        return new EntryResponse(entry.id(), entry.description(), entry.currency().name(), postings);
    }
}
