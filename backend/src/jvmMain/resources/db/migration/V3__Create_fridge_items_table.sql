CREATE TABLE IF NOT EXISTS fridge_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    quantity DOUBLE PRECISION NOT NULL DEFAULT 0,
    calories_per_portion INTEGER NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fridge_items_user_id ON fridge_items(user_id);
