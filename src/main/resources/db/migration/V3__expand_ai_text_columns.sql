ALTER TABLE ai_responses
    ALTER COLUMN answer_text TYPE TEXT;

ALTER TABLE ai_queries
    ALTER COLUMN query_text TYPE TEXT;
