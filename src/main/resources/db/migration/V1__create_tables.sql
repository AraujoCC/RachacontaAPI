-- =========================
-- USERS
-- =========================
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =========================
-- GROUPS
-- =========================
CREATE TABLE groups
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =========================
-- GROUP MEMBERS
-- =========================
CREATE TABLE group_members
(
    id        UUID PRIMARY KEY,
    group_id  UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    role      VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_group_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_group_user UNIQUE (group_id, user_id)
);

-- =========================
-- EXPENSES
-- =========================
CREATE TABLE expenses
(
    id          UUID PRIMARY KEY,
    group_id    UUID           NOT NULL,
    paid_by     UUID           NOT NULL,
    description VARCHAR(255)   NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    split_type  VARCHAR(20)    NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_expenses_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_expenses_paid_by
        FOREIGN KEY (paid_by)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- =========================
-- EXPENSE SPLITS
-- =========================
CREATE TABLE expense_splits
(
    id          UUID PRIMARY KEY,
    expense_id  UUID           NOT NULL,
    user_id     UUID           NOT NULL,
    amount_owed DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_splits_expense
        FOREIGN KEY (expense_id)
            REFERENCES expenses (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_splits_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_expense_user UNIQUE (expense_id, user_id)
);

-- =========================
-- SETTLEMENTS (QUITAÇÕES)
-- =========================
CREATE TABLE settlements
(
    id          UUID PRIMARY KEY,
    group_id    UUID           NOT NULL,
    payer_id    UUID           NOT NULL,
    receiver_id UUID           NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_settlements_group
        FOREIGN KEY (group_id)
            REFERENCES groups (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_settlements_payer
        FOREIGN KEY (payer_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_settlements_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);