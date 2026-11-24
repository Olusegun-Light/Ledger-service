ALTER TABLE accounts
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE accounts
    ADD CONSTRAINT accounts_status_check CHECK ( status IN ('ACTIVE', 'FROZEN', 'CLOSED') );
