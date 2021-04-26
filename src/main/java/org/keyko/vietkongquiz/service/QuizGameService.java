package org.keyko.vietkongquiz.service;

import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keyko.vietkongquiz.Quiz;
import org.keyko.vietkongquiz.dto.VkPostDTO;
import org.keyko.vietkongquiz.entity.HandledPost;
import org.keyko.vietkongquiz.entity.QuizGame;
import org.keyko.vietkongquiz.entity.RoundResult;
import org.keyko.vietkongquiz.entity.TeamResult;
import org.keyko.vietkongquiz.repository.HandledPostRepository;
import org.keyko.vietkongquiz.repository.QuizGameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizGameService {
    private final QuizGameRepository quizGameRepository;
    private final HandledPostRepository handledPostRepository;
    private final Pattern isNumericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final Pattern teamResultPattern = Pattern.compile("^(1 ).+");

    public QuizGame saveQuizGame(Quiz quiz, VkPostDTO vkPostDTO, String tableString) {
        log.info("Start handling game from file <{}>.", vkPostDTO.getQuizTableImage().getFile().getName());
        WallpostFull post = vkPostDTO.getPost();
        QuizGame quizGame = new QuizGame();
        LocalDate postDate = Instant.ofEpochSecond(post.getDate()).atOffset(ZoneOffset.UTC).toLocalDate();
        quizGame.setGameDate(postDate.minusDays(1));
        quizGame.setGameType(quiz.getGameType());
        List<String> resultStringList = convertTableStringToResultStringList(tableString);
        List<TeamResult> teamResultList = resultStringList
                .stream()
                .map(this::convertFromResultString)
                .collect(Collectors.toList());
        teamResultList.forEach(teamResult -> teamResult.setQuizGame(quizGame));
        quizGame.setTeamResults(teamResultList);
        quizGame.setTableUrl(vkPostDTO.getQuizTableImage().getFile().getAbsolutePath());

        QuizGame savedQuizGame = quizGameRepository.save(quizGame);
        logQuizGameSaved(quizGame);
        HandledPost handledPost = new HandledPost();
        handledPost.setGameType(quizGame.getGameType());
        handledPost.setPostDate(postDate);
        handledPost.setPostId(post.getId());
        handledPostRepository.save(handledPost);

        return savedQuizGame;
    }

    private List<String> convertTableStringToResultStringList(String tableString) {
        String[] resultStringArray = tableString
                .replaceAll("\\s[OoОо]\\s", " 0 ")
                .replaceAll("\\s~", " -")
                .split("\\n");
        if (!teamResultPattern.matcher(resultStringArray[0]).matches()) {
            if (!teamResultPattern.matcher(resultStringArray[1]).matches()) {
                return Arrays
                        .stream(resultStringArray)
                        .skip(2)
                        .filter(resultString -> resultString.split(" ").length > 8)
                        .collect(Collectors.toList());
            } else {
                return Arrays
                        .stream(resultStringArray)
                        .skip(1)
                        .filter(resultString -> resultString.split(" ").length > 8)
                        .collect(Collectors.toList());
            }
        } else {
            return Arrays.stream(resultStringArray)
                    .filter(resultString -> resultString.split(" ").length > 8)
                    .collect(Collectors.toList());
        }
    }

    private TeamResult convertFromResultString(String resultString) {
        log.debug("Converting resultString <{}> to team result...", resultString);
        TeamResult teamResult = new TeamResult();
        String placeString = resultString.substring(0, resultString.indexOf(" "));

        if (isStringNumeric(placeString)) {
            teamResult.setPlace(Short.parseShort(placeString));
        } else {
            log.error("Error parsing place from resultString <{}>, set as <0>", resultString);
            teamResult.setPlace((short) 0);
            teamResult.setNeedManualCorrection(true);
        }

        String summaryString =
                fixIncorrectOCRInNumericString(resultString.substring(resultString.lastIndexOf(" ") + 1));

        if (isStringNumeric(summaryString)) {
            teamResult.setSummary(new BigDecimal(summaryString).setScale(2, RoundingMode.DOWN));
        } else {
            log.error("Error parsing summary from resultString <{}>, set as <0>", resultString);
            teamResult.setSummary(new BigDecimal(0).setScale(2, RoundingMode.DOWN));
            teamResult.setNeedManualCorrection(true);
        }

        String stringWithoutPlaceAndSummary = resultString.substring(resultString.indexOf(" ") + 1, resultString.lastIndexOf(" "));
        String teamName = getTeamNameFromString(stringWithoutPlaceAndSummary);
        teamResult.setTeamName(teamName);
        String roundResultString = stringWithoutPlaceAndSummary
                .substring(stringWithoutPlaceAndSummary.indexOf(teamName) + teamName.length() + 1);
        List<RoundResult> roundResultList = getResultByRoundListFromString(roundResultString);
        roundResultList.forEach(roundResult -> roundResult.setTeamResult(teamResult));

        teamResult.setRoundResultList(roundResultList);
        BigDecimal summaryByRounds = new BigDecimal(0).setScale(2, RoundingMode.DOWN);

        for (RoundResult roundResult : roundResultList) {
            summaryByRounds = summaryByRounds.add(roundResult.getResult());
        }

        if (!summaryByRounds.equals(teamResult.getSummary())) {
            teamResult.setNeedManualCorrection(true);
        }

        return teamResult;
    }

    private List<RoundResult> getResultByRoundListFromString(String roundResultString) {
        log.debug("Converting roundResultString <{}> to List<RoundResult>...", roundResultString);
        String[] results = roundResultString.split(" ");
        List<RoundResult> roundResultList = new ArrayList<>();

        for (short i = 0; i < results.length; i++) {
            RoundResult roundResult = new RoundResult();
            roundResult.setRound((short) (i + 1));
            results[i] = fixIncorrectOCRInNumericString(results[i]);

            if (isStringNumeric(results[i])) {
                roundResult.setResult(new BigDecimal(results[i]).setScale(2, RoundingMode.DOWN));
            } else {
                log.error("Error parsing result from resultString <{}> ({}), set as <0>", results[i], roundResultString);
                roundResult.setResult(new BigDecimal(0).setScale(2, RoundingMode.DOWN));
            }
            roundResultList.add(roundResult);
        }

        return roundResultList;
    }

    private String getTeamNameFromString(String string) {
        String[] parts = string.split(" ");
        StringBuilder teamName = new StringBuilder(parts[0]);

        for (int i = 1; i < parts.length - 1; i++) {
            if (isStringNumeric(parts[i]) && isStringNumeric(parts[i + 1])) {
                break;
            } else {
                teamName.append(" ").append(parts[i]);
            }
        }

        return teamName.toString();
    }

    private boolean isStringNumeric(String string) {
        if (string == null) {
            return false;
        }

        return isNumericPattern.matcher(string).matches();
    }

    private String fixIncorrectOCRInNumericString(String numericString) {
        return numericString
                .replaceAll("a", "4")
                .replaceAll("а", "4")
                .replaceAll("T", "7")
                .replaceAll("Т", "7")
                .replaceAll("g", "9")
                .replaceAll(",", ".")
                .replaceAll("~", "-");

    }

    private void logQuizGameSaved(QuizGame quizGame) {
        List<TeamResult> teamResultsNeedManualCorrection =
                quizGame.getTeamResults()
                        .stream()
                        .filter(TeamResult::isNeedManualCorrection)
                        .collect(Collectors.toList());
        if (teamResultsNeedManualCorrection.size() > 0) {
            log.info(
                    "QuizGame <{}> saved, but {} team results need manual correction ({}).",
                    quizGame,
                    teamResultsNeedManualCorrection.size(),
                    teamResultsNeedManualCorrection.stream().map(TeamResult::getTeamResultId).collect(Collectors.toList())
            );
        } else {
            log.info("QuizGame <{}> saved.", quizGame);
        }
    }
}
