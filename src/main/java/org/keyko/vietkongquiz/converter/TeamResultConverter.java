package org.keyko.vietkongquiz.converter;

import lombok.RequiredArgsConstructor;
import org.keyko.vietkongquiz.dto.TeamResultDTO;
import org.keyko.vietkongquiz.entity.TeamResult;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamResultConverter {

    private final RoundResultConverter roundResultConverter;

    public TeamResultDTO convert(TeamResult teamResult) {
        return new TeamResultDTO(
                teamResult.getTeamResultId(),
                teamResult.getTeamName(),
                teamResult.getQuizGame().getGameId(),
                teamResult.getPlace(),
                teamResult.getSummary().toPlainString(),
                teamResult.isNeedManualCorrection(),
                teamResult.getRoundResultList()
                        .stream()
                        .map(roundResultConverter::convert)
                        .collect(Collectors.toList())
        );
    }
}
