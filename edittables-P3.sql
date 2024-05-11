
CREATE TABLE IF NOT EXISTS employees(
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);

INSERT INTO employees (email, password, fullname) VALUES ("classta@email.edu", "classta", "andTA CS122B");


CREATE TABLE IF NOT EXISTS id_manager (
                            table_name VARCHAR(50) NOT NULL,
                            next_id INT NOT NULL,
                            PRIMARY KEY (table_name)
);


INSERT INTO id_manager (table_name, next_id) SELECT 'stars', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM stars;

INSERT INTO id_manager (table_name, next_id) SELECT 'movies', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM movies;


DELIMITER //

DROP PROCEDURE IF EXISTS add_star;
CREATE PROCEDURE `add_star`(
    IN in_name VARCHAR(100),
    IN in_year INT,
    OUT out_id VARCHAR(10)
)
BEGIN
    DECLARE nextId INT;

    -- Start a transaction
    START TRANSACTION;

    -- Get the next available ID from the id_manager table
    SELECT next_id INTO nextId FROM id_manager WHERE table_name = 'stars' FOR UPDATE;

    -- Construct the new ID with the 'nm' prefix
    SET out_id = CONCAT('nm', nextId);

    -- Insert the new star with the generated ID
    INSERT INTO stars (id, name, birthYear) VALUES (out_id, in_name, in_year);

    -- Increment the next_id in id_manager
    UPDATE id_manager SET next_id = next_id + 1 WHERE table_name = 'stars';

COMMIT;


DROP PROCEDURE IF EXISTS add_movie;
CREATE PROCEDURE `add_movie`(
    IN in_title VARCHAR(100),
    IN in_director VARCHAR(100),
    IN in_year INT,
    IN in_genre VARCHAR(32),
    IN in_star_name VARCHAR(100),
    OUT out_movie_id VARCHAR(10),
    OUT out_genre_id INT,
    OUT out_star_id VARCHAR(10),
    OUT out_status VARCHAR(100)
)
BEGIN
    DECLARE next_movie_id INT;
    DECLARE existing_genre_id INT;


    -- Check if the movie already exists, matching title year and director
    SELECT id INTO next_movie_id FROM movies WHERE title = in_title AND director = in_director AND year = in_year LIMIT 1;

    -- If movie doesn't exist add it to database and update id manager
    IF next_movie_id IS NULL THEN
        -- Start a transaction
        START TRANSACTION;

        -- Get the next available movie ID from the id_manager table
        SELECT next_id INTO next_movie_id FROM id_manager WHERE table_name = 'movies' FOR UPDATE;

        -- Construct the new movie ID with the 'mv' prefix
        SET out_movie_id = CONCAT('tt', next_movie_id);

        -- Insert the new movie with the generated ID
        INSERT INTO movies (id, title, director, year) VALUES (out_movie_id, in_title, in_director, in_year);

        -- Increment the next_id in id_manager
        UPDATE id_manager SET next_id = next_id + 1 WHERE table_name = 'movies';


        -- Check if the genre already exists, and insert if it doesnt'
        SELECT id INTO existing_genre_id FROM genres WHERE name = in_genre LIMIT 1;
        IF existing_genre_id IS NULL THEN
                -- Insert new genre and get the new ID
                INSERT INTO genres (name) VALUES (in_genre);
                -- Since auto generated, can get new id using last_insert_id()
                SET out_genre_id = LAST_INSERT_ID();
        ELSE
                -- Set out_genre_id to the existing genre's ID
                SET out_genre_id = existing_genre_id;
        END IF;

        -- Insert into genres_in_movies
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (out_genre_id, out_movie_id);


        -- Now call add_star to handle adding a star
        CALL add_star(in_star_name, NULL, out_star_id);

        -- Insert into stars_in_movies
        INSERT INTO stars_in_movies (starId, movieId) VALUES (out_star_id, out_movie_id);

        SET out_status = 'Movie added correctly';

        COMMIT;
    ELSE
        -- Duplicate movie, Return with no action taken
        SET out_status = 'Movie already exists';

    END IF;

END //


DELIMITER ;