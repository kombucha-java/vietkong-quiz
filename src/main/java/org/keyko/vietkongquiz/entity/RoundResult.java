package org.keyko.vietkongquiz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "round_results")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoundResult {
    @Id
    @Column(name = "round_result_id", updatable = false)
    @SequenceGenerator(
            name="round_results_round_result_id_seq",
            sequenceName="round_results_round_result_id_seq",
            allocationSize=1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator="round_results_round_result_id_seq"
    )
    private Long resultByRoundId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "team_result_id")
    private TeamResult teamResult;

    @Column(name = "round")
    private short round;

    @Column(name = "result")
    private BigDecimal result;
}
