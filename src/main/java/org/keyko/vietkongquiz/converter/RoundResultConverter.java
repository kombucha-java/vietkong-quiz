package org.keyko.vietkongquiz.converter;

import org.keyko.vietkongquiz.dto.RoundResultDTO;
import org.keyko.vietkongquiz.entity.RoundResult;
import org.springframework.stereotype.Component;

@Component
public class RoundResultConverter {
    public RoundResultDTO convert(RoundResult roundResult) {
        return new RoundResultDTO(
                roundResult.getResultByRoundId(),
                roundResult.getTeamResult().getTeamResultId(),
                roundResult.getRound(),
                roundResult.getResult().toPlainString()
        );
    }
}
