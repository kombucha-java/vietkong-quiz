package org.keyko.vietkongquiz.dto;

import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.Data;

@Data
public class VkPostDTO {
    private final WallpostFull post;
    private final QuizTableImage quizTableImage;
}
