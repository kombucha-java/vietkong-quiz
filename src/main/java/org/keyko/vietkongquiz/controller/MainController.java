package org.keyko.vietkongquiz.controller;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keyko.vietkongquiz.Quiz;
import org.keyko.vietkongquiz.converter.QuizGameConverter;
import org.keyko.vietkongquiz.dto.FullScanDTO;
import org.keyko.vietkongquiz.dto.QuizGameDTO;
import org.keyko.vietkongquiz.dto.TeamResultDTO;
import org.keyko.vietkongquiz.dto.VkPostDTO;
import org.keyko.vietkongquiz.service.OCRService;
import org.keyko.vietkongquiz.service.QuizGameService;
import org.keyko.vietkongquiz.service.VkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {
    private final VkService vkService;
    private final OCRService ocrService;
    private final QuizGameService quizGameService;
    private final QuizGameConverter quizGameConverter;

    @GetMapping("/get_tables_wowquiz")
    public List<QuizGameDTO> getPhotosWowquiz(@RequestParam Integer count) throws ClientException, ApiException {
        List<VkPostDTO> vkPostDTOList = vkService.getVkPostDTOList(Quiz.WOW_QUIZ_NSK, count, 0);

        return createQuizGames(vkPostDTOList, Quiz.WOW_QUIZ_NSK);
    }

    @GetMapping("/get_tables_einstein")
    public List<QuizGameDTO> getPhotosEinstein(@RequestParam Integer count) throws ClientException, ApiException {
        List<VkPostDTO> vkPostDTOList = vkService.getVkPostDTOList(Quiz.EINSTEIN_PARTY_NSK, count, 0);

        return createQuizGames(vkPostDTOList, Quiz.EINSTEIN_PARTY_NSK);
    }

    @GetMapping("/fullscan_wowquiz")
    public FullScanDTO fullScanWowQuiz() throws ClientException, ApiException {
        Quiz quiz = Quiz.WOW_QUIZ_NSK;

        return performFullScan(quiz);
    }

    @GetMapping("/fullscan_einstein")
    public FullScanDTO fullScanEinstein() throws ClientException, ApiException {
        Quiz quiz = Quiz.EINSTEIN_PARTY_NSK;

        return performFullScan(quiz);
    }

    private FullScanDTO performFullScan(Quiz quiz) throws ApiException, ClientException {
        FullScanDTO fullScanDTO = new FullScanDTO();
        fullScanDTO.setGameType(quiz.getGameType());

        int postsCount = vkService.getPostsCount(quiz);
        fullScanDTO.setPostCount(postsCount);

        List<VkPostDTO> vkPostDTOList = vkService.fullScan(quiz, postsCount);
        List<QuizGameDTO> quizGameDTOList = createQuizGames(vkPostDTOList, quiz);
        fullScanDTO.setGamesHandledCount(quizGameDTOList.size());
        int gamesHandledWithErrorsCount = (int) quizGameDTOList
                .stream()
                .filter(quizGameDTO -> quizGameDTO.getTeamResults().stream().anyMatch(TeamResultDTO::isNeedManualCorrection))
                .count();
        fullScanDTO.setGamesHandledWithErrorsCount(gamesHandledWithErrorsCount);
        /*try {
            String pathToPosts = "/home/alexey/posts.einstein.txt";
            File newFile = new File(pathToPosts);
            FileUtils.touch(newFile);
            vkPostDTOList.forEach(vkPostDTO -> {
                try {
                    FileUtils.writeStringToFile(
                            newFile,
                            vkPostDTO.getPost().getText() + "\n=================\n",
                            StandardCharsets.UTF_8,
                            true);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }*/
        return fullScanDTO;
    }


    private List<QuizGameDTO> createQuizGames(List<VkPostDTO> vkPostDTOList, Quiz quiz) {
        return vkPostDTOList
                .stream()
                .map(dto -> {
                    String tableString = ocrService.doOcr(dto.getQuizTableImage(), quiz);
                    return quizGameService.saveQuizGame(quiz, dto, tableString);
                })
                .map(quizGameConverter::convert)
                .collect(Collectors.toList());
    }
}
