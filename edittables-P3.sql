
CREATE TABLE IF NOT EXISTS employees(
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);

INSERT INTO employees (email, password, fullname) VALUES ("classta@email.edu", "classta", "andTA CS122B");


CREATE TABLE id_manager (
                            table_name VARCHAR(50) NOT NULL,
                            next_id INT NOT NULL,
                            PRIMARY KEY (table_name)
);

INSERT INTO id_manager (table_name, next_id) SELECT 'stars', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM stars;


DELIMITER //

CREATE PROCEDURE `AddStar`(
    IN starName VARCHAR(100),
    IN birthYear INT,
    OUT starId VARCHAR(10)
)
BEGIN
    DECLARE nextId INT;

    -- Start a transaction
    START TRANSACTION;

    -- Get the next available ID from the id_manager table
    SELECT next_id INTO nextId FROM id_manager WHERE table_name = 'stars' FOR UPDATE;

    -- Construct the new ID with the 'nm' prefix
    SET starId = CONCAT('nm', nextId);

    -- Insert the new star with the generated ID
    INSERT INTO stars (id, name, birthYear) VALUES (starId, starName, birthYear);

    -- Increment the next_id in id_manager
    UPDATE id_manager SET next_id = next_id + 1 WHERE table_name = 'stars';

    COMMIT;
END //


DELIMITER ;