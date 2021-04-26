package org.keyko.vietkongquiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamResultDTO {
    private long teamResultId;
    private String teamName;
    private long gameId;
    private short place;
    private String summary;
    private boolean needManualCorrection;
    private List<RoundResultDTO> roundResults;
}
