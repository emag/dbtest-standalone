DROP TABLE IF EXISTS test_table;

CREATE TABLE test_table (
 id integer NOT NULL,
 value integer NOT NULL,
 PRIMARY KEY (id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO test;