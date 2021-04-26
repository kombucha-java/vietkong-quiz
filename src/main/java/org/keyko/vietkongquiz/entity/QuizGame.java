package org.keyko.vietkongquiz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name="quiz_games")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizGame {
    @Id
    @Column(name = "game_id", updatable = false)
    @SequenceGenerator(
            name="quiz_games_game_id_seq",
            sequenceName="quiz_games_game_id_seq",
            allocationSize=1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator="quiz_games_game_id_seq"
    )
    private Long gameId;

    @Column(name = "game_date")
    private LocalDate gameDate;

    @Column(name = "game_type")
    private String gameType;

    @Column(name = "table_url")
    private String tableUrl;

    @OneToMany(mappedBy = "quizGame", cascade = CascadeType.ALL)
    private List<TeamResult> teamResults;

    @Override
    public String toString() {
        return String.format("{game_id=%s, gameDate=%s, gameType=%s}", gameId, gameDate, gameType);
    }
}
