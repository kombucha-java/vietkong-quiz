package org.keyko.vietkongquiz.service;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostAttachmentType;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.queries.wall.WallGetQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keyko.vietkongquiz.Quiz;
import org.keyko.vietkongquiz.download.PhotoDownloader;
import org.keyko.vietkongquiz.dto.VkPostDTO;
import org.keyko.vietkongquiz.dto.QuizTableImage;
import org.keyko.vietkongquiz.entity.HandledPost;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VkService {
    public static final String ACCESS_TOKEN =
            "";
    public static final Integer USER_ID = 0;

    private final PhotoDownloader photoDownloader;
    private final HandledPostService handledPostService;

    public List<VkPostDTO> fullScan(Quiz quiz, int postsCount) throws ClientException, ApiException {
        log.info("Full scan of <{}> started ...", quiz.getGameType());
        int offset = 0;
        List<VkPostDTO> vkPostDTOList = new ArrayList<>();

        while (offset < postsCount) {
            vkPostDTOList.addAll(getVkPostDTOList(quiz, 100, offset));
            offset = offset + 100;
        }

        return vkPostDTOList;
    }

    public Integer getPostsCount(Quiz quiz) throws ApiException, ClientException {
        return getWallGetQuery(quiz.getDomain(), 0, 0).execute().getCount();
    }

    public List<VkPostDTO> getVkPostDTOList(Quiz quiz, int count, int offset) throws ClientException, ApiException {
        log.info("Start handle <{}> <{}> posts...", quiz.getGameType(), count);
        GetResponse response = getWallGetQuery(quiz.getDomain(), count, offset).execute();
        List<WallpostFull> posts = response.getItems();
        List<HandledPost> handledPosts = handledPostService.findByGameType(quiz.getGameType());

        return posts.stream()
                .filter(this::filterVkPostsByContent)
                .filter(post -> isVkPostNotHandled(handledPosts, post))
                .map(post -> getVkPostDTOList(post, quiz))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private boolean isVkPostNotHandled(List<HandledPost> handledPosts, WallpostFull post) {
        LocalDate postDate = Instant.ofEpochSecond(post.getDate()).atOffset(ZoneOffset.UTC).toLocalDate();

        return handledPosts
                .stream()
                .noneMatch(handledPost -> handledPost.getPostId() == post.getId() &&
                        handledPost.getPostDate().equals(postDate));
    }

    private boolean filterVkPostsByContent(WallpostFull post) {
        return post.getText().contains("\uD83E\uDD47") ||
                post.getText().contains("\uD83E\uDD48") ||
                post.getText().contains("\uD83E\uDD49") ||
                post.getText().contains("1 место") ||
                post.getText().contains("2 место") ||
                post.getText().contains("3 место");
    }

    private List<VkPostDTO> getVkPostDTOList(WallpostFull post, Quiz quiz) {
        log.info("Handle <{}> post of <{}>...", post.getId(), quiz.getGameType());
        List<QuizTableImage> quizTableImages = post.getAttachments()
                .stream()
                .filter(attachment -> attachment.getType().equals(WallpostAttachmentType.PHOTO))
                .map(attachment -> {
                    String fileName = String.join(
                            "_",
                            quiz.getGameType(),
                            post.getDate().toString(),
                            post.getId().toString(),
                            UUID.randomUUID().toString()
                    );

                    return getQuizTableImage(attachment, fileName);
                })
                .collect(Collectors.toList());

        return quizTableImages.stream()
                .map(quizTableImage -> new VkPostDTO(post, quizTableImage))
                .collect(Collectors.toList());
    }

    private QuizTableImage getQuizTableImage(WallpostAttachment attachment, String fileName) {
        Photo photo = Objects.requireNonNull(attachment.getPhoto());
        PhotoSizes photoSizes = photo.getSizes()
                .stream()
                .max(Comparator.comparing(PhotoSizes::getHeight))
                .orElseThrow(NoSuchElementException::new);
        URI uri = photoSizes.getUrl();
        File downloadedFile = photoDownloader.download(uri, fileName);
        log.info("File {} saved to FS.", downloadedFile.getAbsolutePath());

        return new QuizTableImage(downloadedFile, photoSizes.getHeight(), photoSizes.getWidth());
    }

    private WallGetQuery getWallGetQuery(String domain, int count, int offset) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = new UserActor(USER_ID, ACCESS_TOKEN);

        return vk.wall().get(actor)
                .domain(domain)
                .count(count)
                .offset(offset);
    }
}
