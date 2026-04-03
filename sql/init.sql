-- PostgreSQL init script for SGBD Lab 1
-- Creates:
-- - 1-n: shows -> episodes
-- - m-n: shows <-> actors (show_actors)


DROP TABLE IF EXISTS showsactors;
DROP TABLE IF EXISTS show_actors;
DROP TABLE IF EXISTS episodes;
DROP TABLE IF EXISTS actors;
DROP TABLE IF EXISTS shows;

CREATE TABLE shows (
    s_id    SERIAL PRIMARY KEY,
    s_title TEXT NOT NULL UNIQUE
);

CREATE TABLE episodes (
    e_id    SERIAL PRIMARY KEY,
    s_id    INTEGER NOT NULL REFERENCES shows(s_id) ON DELETE RESTRICT,
    e_title TEXT NOT NULL
);

CREATE TABLE actors (
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE show_actors (
    s_id     INTEGER NOT NULL REFERENCES shows(s_id) ON DELETE CASCADE,
    actor_id INTEGER NOT NULL REFERENCES actors(id) ON DELETE CASCADE,
    PRIMARY KEY (s_id, actor_id)
);

-- Seed data (min 3 parents, 5-10 children)
INSERT INTO shows (s_title) VALUES
('Breaking Bad'),
('Stranger Things'),
('Sherlock');

INSERT INTO episodes (s_id, e_title) VALUES
((SELECT s_id FROM shows WHERE s_title = 'Breaking Bad'), 'Pilot'),
((SELECT s_id FROM shows WHERE s_title = 'Breaking Bad'), 'Cat''s in the Bag...'),
((SELECT s_id FROM shows WHERE s_title = 'Stranger Things'), 'The Vanishing of Will Byers'),
((SELECT s_id FROM shows WHERE s_title = 'Stranger Things'), 'The Weirdo on Maple Street'),
((SELECT s_id FROM shows WHERE s_title = 'Sherlock'), 'A Study in Pink'),
((SELECT s_id FROM shows WHERE s_title = 'Sherlock'), 'The Blind Banker');

INSERT INTO actors (name) VALUES
('Bryan Cranston'),
('Aaron Paul'),
('Millie Bobby Brown'),
('Benedict Cumberbatch');

INSERT INTO show_actors (s_id, actor_id) VALUES
((SELECT s_id FROM shows WHERE s_title = 'Breaking Bad'), (SELECT id FROM actors WHERE name = 'Bryan Cranston')),
((SELECT s_id FROM shows WHERE s_title = 'Breaking Bad'), (SELECT id FROM actors WHERE name = 'Aaron Paul')),
((SELECT s_id FROM shows WHERE s_title = 'Stranger Things'), (SELECT id FROM actors WHERE name = 'Millie Bobby Brown')),
((SELECT s_id FROM shows WHERE s_title = 'Sherlock'), (SELECT id FROM actors WHERE name = 'Benedict Cumberbatch'));

