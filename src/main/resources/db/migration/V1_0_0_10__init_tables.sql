CREATE TABLE quiz_games
(
    game_id   BIGSERIAL    NOT NULL PRIMARY KEY,
    game_date DATE         NOT NULL,
    game_type VARCHAR(100) NOT NULL,
    table_url VARCHAR(256) NOT NULL
);

CREATE TABLE team_results
(
    team_result_id         BIGSERIAL PRIMARY KEY,
    team_name              VARCHAR(100) NOT NULL,
    game_id                BIGINT       NOT NULL,
    place                  SMALLINT     NOT NULL,
    summary                SMALLINT     NOT NULL,
    need_manual_correction BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (game_id) REFERENCES quiz_games (game_id)
);

CREATE TABLE round_results
(
    round_result_id BIGSERIAL PRIMARY KEY,
    team_result_id     BIGINT       NOT NULL,
    round              SMALLINT     NOT NULL,
    result             SMALLINT     NOT NULL,
    FOREIGN KEY (team_result_id) REFERENCES team_results (team_result_id)
);

