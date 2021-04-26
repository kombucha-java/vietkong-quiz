package org.keyko.vietkongquiz.dto;

import lombok.Data;

import java.io.File;

@Data
public class QuizTableImage {
    final File file;
    final int height;
    final int width;
}
