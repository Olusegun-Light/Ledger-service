package com.olusegunlight.ledger.common.exception;

import java.io.Serializable;

public record FieldViolation(String field, String message) {
}
