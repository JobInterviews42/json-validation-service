-- Json Schema


-- !Ups
CREATE TABLE JsonSchema (
    id TEXT PRIMARY KEY,
    rawSchema TEXT NOT NULL
);


-- !Downs

DROP TABLE JsonSchema;