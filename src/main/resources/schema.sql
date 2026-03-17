CREATE TABLE IF NOT EXISTS shows (
    s_id SERIAL PRIMARY KEY,
    s_title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS episodes (
    e_id SERIAL PRIMARY KEY,
    s_id INTEGER NOT NULL REFERENCES shows (s_id) ON DELETE CASCADE,
    e_title VARCHAR(255) NOT NULL
);

INSERT INTO shows (s_title) VALUES
    ('Sample Show 1'),
    ('Sample Show 2'),
    ('Sample Show 3')
ON CONFLICT DO NOTHING;

INSERT INTO episodes (s_id, e_title) VALUES
    (1, 'Episode 1'),
    (1, 'Episode 2'),
    (2, 'Episode A'),
    (2, 'Episode B'),
    (3, 'Pilot')
ON CONFLICT DO NOTHING;

