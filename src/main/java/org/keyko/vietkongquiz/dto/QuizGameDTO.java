package org.keyko.vietkongquiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizGameDTO {
    private long gameId;
    private LocalDate gameDate;
    private String gameType;
    private String tableUrl;
    private List<TeamResultDTO> teamResults;
}
