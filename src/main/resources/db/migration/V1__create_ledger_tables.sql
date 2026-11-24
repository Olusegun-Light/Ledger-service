CREATE TABLE accounts
(
    id         VARCHAR(64) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(16)  NOT NULL,
    currency   CHAR(3)      NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT accounts_type_check CHECK (type IN ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE'))
);

CREATE TABLE journal_entries
(
    id          VARCHAR(64) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    currency    CHAR(3)      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE postings
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    journal_entry_id VARCHAR(64) NOT NULL REFERENCES journal_entries (id),
    account_id       VARCHAR(64) NOT NULL REFERENCES accounts (id),
    direction        VARCHAR(6)  NOT NULL,
    amount_minor     BIGINT      NOT NULL,
    CONSTRAINT postings_direction_check CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT postings_amount_positive CHECK (amount_minor > 0)
);