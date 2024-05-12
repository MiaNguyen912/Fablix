
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

