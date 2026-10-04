ALTER TABLE users ADD COLUMN IF NOT EXISTS time_zone_id TEXT NOT NULL DEFAULT 'UTC';
ALTER TABLE fridge_items ADD COLUMN IF NOT EXISTS added_date DATE;
UPDATE fridge_items SET added_date = created_at::date WHERE added_date IS NULL;

CREATE TABLE IF NOT EXISTS health_dashboard_snapshots (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_date DATE NOT NULL,
    time_zone_id TEXT NOT NULL,
    produce_count INTEGER NOT NULL DEFAULT 0,
    protein_count INTEGER NOT NULL DEFAULT 0,
    grain_count INTEGER NOT NULL DEFAULT 0,
    other_count INTEGER NOT NULL DEFAULT 0,
    unclassified_count INTEGER NOT NULL DEFAULT 0,
    total_tracked_items INTEGER NOT NULL DEFAULT 0,
    diversity_score INTEGER NOT NULL DEFAULT 0,
    diversity_rating TEXT NOT NULL DEFAULT 'LOW',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, snapshot_date)
);

CREATE TABLE IF NOT EXISTS health_dashboard_expiry_snapshots (
    user_id UUID NOT NULL,
    snapshot_date DATE NOT NULL,
    item_id UUID NOT NULL,
    item_name TEXT NOT NULL,
    category TEXT NOT NULL,
    expiry_date DATE NOT NULL,
    PRIMARY KEY (user_id, snapshot_date, item_id),
    FOREIGN KEY (user_id, snapshot_date)
        REFERENCES health_dashboard_snapshots(user_id, snapshot_date) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS health_dashboard_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_name TEXT NOT NULL,
    payload TEXT NOT NULL DEFAULT '{}',
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_health_dashboard_snapshots_user_date
    ON health_dashboard_snapshots(user_id, snapshot_date DESC);
CREATE INDEX IF NOT EXISTS idx_health_dashboard_events_user_time
    ON health_dashboard_events(user_id, occurred_at DESC);
