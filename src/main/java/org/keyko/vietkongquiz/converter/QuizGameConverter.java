package org.keyko.vietkongquiz.converter;

import lombok.RequiredArgsConstructor;
import org.keyko.vietkongquiz.dto.QuizGameDTO;
import org.keyko.vietkongquiz.entity.QuizGame;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizGameConverter {

    private final TeamResultConverter teamResultConverter;

    public QuizGameDTO convert(QuizGame quizGame) {
        return new QuizGameDTO(
                quizGame.getGameId(),
                quizGame.getGameDate(),
                quizGame.getGameType(),
                quizGame.getTableUrl(),
                quizGame.getTeamResults()
                        .stream()
                        .map(teamResultConverter::convert)
                        .collect(Collectors.toList())
        );
    }
}
