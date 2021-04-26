package org.keyko.vietkongquiz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "team_results")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamResult {
    @Id
    @Column(name = "team_result_id", updatable = false)
    @SequenceGenerator(
            name="team_results_team_result_id_seq",
            sequenceName="team_results_team_result_id_seq",
            allocationSize=1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator="team_results_team_result_id_seq"
    )
    private Long teamResultId;

    @Column(name = "team_name")
    private String teamName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private QuizGame quizGame;

    @Column(name = "place")
    private short place;

    @Column(name = "need_manual_correction")
    private boolean needManualCorrection;

    @Column(name = "summary")
    private BigDecimal summary;

    @OneToMany(mappedBy = "teamResult", cascade = CascadeType.ALL)
    private List<RoundResult> roundResultList;
}
