CREATE TABLE recipe_feedback (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    recipe_id TEXT NOT NULL,
    feedback_type TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT recipe_feedback_unique_user_recipe UNIQUE (user_id, recipe_id)
);
