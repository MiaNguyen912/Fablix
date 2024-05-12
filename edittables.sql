ALTER TABLE movies ADD COLUMN price INT NOT NULL DEFAULT 0;

UPDATE movies SET price = FLOOR(RAND() * 60) + 1;

ALTER TABLE sales ADD COLUMN Quantity INT NOT NULL DEFAULT 1;

INSERT INTO employees (email, password, fullname) VALUES ("classta@email.edu", "classta", "andTA CS122B");

-- Creating index to improve query performance:

-- ratings
CREATE INDEX idx_ratings_movieid ON ratings (movieid);

-- movies
CREATE INDEX idx_movies_id ON movies (id);

-- genres_in_movies
CREATE INDEX idx_genres_in_movies_movieid ON genres_in_movies (movieid);
CREATE INDEX idx_genres_in_movies_genreid ON genres_in_movies (genreid);

-- genres
CREATE INDEX idx_genres_name ON genres (name);

-- stars_in_movies
CREATE INDEX idx_stars_in_movies_movieid ON stars_in_movies (movieid);
CREATE INDEX idx_stars_in_movies_starid ON stars_in_movies (starid);

-- stars
CREATE INDEX idx_stars_id ON stars (id);