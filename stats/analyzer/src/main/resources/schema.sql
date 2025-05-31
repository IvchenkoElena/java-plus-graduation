CREATE SCHEMA IF NOT EXISTS analyzer_schema;

CREATE TABLE IF NOT EXISTS analyzer_schema.event_similarity
(
    id         BIGINT PRIMARY KEY,
    event_a_id BIGINT,
    event_b_id BIGINT,
    score      DOUBLE PRECISION,
    timestamp  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS analyzer_schema.user_actions
(
    id        BIGINT PRIMARY KEY,
    event_id  BIGINT,
    user_id   BIGINT,
    score     DOUBLE PRECISION,
    timestamp TIMESTAMP
);