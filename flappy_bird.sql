DROP TABLE flappy_bird.Scores;
DROP TABLE flappy_bird.Players;

CREATE TABLE flappy_bird.Players (
    username VARCHAR(50) UNIQUE NOT NUll,
    PRIMARY KEY (username)
);

CREATE TABLE flappy_bird.Scores (
	score_id int AUTO_INCREMENT,
    score int NOT NULL,
	username VARCHAR(50),
    PRIMARY KEY (score_id),
    FOREIGN KEY (username) REFERENCES Players (username)
);