CREATE TABLE handled_posts
(
    handled_post_id BIGSERIAL    NOT NULL PRIMARY KEY,
    post_date       DATE         NOT NULL,
    post_id         BIGINT       NOT NULL,
    game_type       VARCHAR(100) NOT NULL
);

