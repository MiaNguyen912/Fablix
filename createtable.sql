
CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

CREATE TABLE IF NOT EXISTS movies(
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL DEFAULT '',
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL DEFAULT ''
    );

CREATE TABLE IF NOT EXISTS stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INTEGER
    );

CREATE TABLE IF NOT EXISTS stars_in_movies(
    starId VARCHAR(10) NOT NULL DEFAULT '',
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
    );

CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL DEFAULT ''
    );

CREATE TABLE IF NOT EXISTS genres_in_movies(
    genreId integer NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
    );

Create table if not exists creditcards (
    Id varchar(20) primary key,
    Firstname varchar(50) NOT NULL DEFAULT '',
    Lastname varchar(50) NOT NULL DEFAULT '',
    Expiration date not null
    );

CREATE TABLE IF NOT EXISTS customers(
    Id integer primary key auto_increment,
    Firstname varchar(50) NOT NULL DEFAULT '',
    Lastname varchar(50) NOT NULL DEFAULT '',
    Ccid varchar(50) NOT NULL DEFAULT '' references creditcards(id),
    Address varchar(200) NOT NULL DEFAULT '',
    Email varchar(50) NOT NULL DEFAULT '',
    Password varchar(20) NOT NULL DEFAULT ''
    );

Create table IF NOT EXISTS sales (
    Id integer primary key auto_increment,
    Customerid integer not null references customers(id),
    Movieid varchar(10) NOT NULL DEFAULT '' references movies(id),
    Saledate Date not null
    );

Create table IF NOT EXISTS ratings (
    Movieid varchar(10) NOT NULL DEFAULT '' references movies(id),
    Rating float not null,
    Numvotes integer not null
    );

CREATE TABLE IF NOT EXISTS employees(
                                        email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
    );

CREATE TABLE IF NOT EXISTS id_manager (
                                          table_name VARCHAR(50) NOT NULL,
    next_id INT NOT NULL,
    PRIMARY KEY (table_name)
    );