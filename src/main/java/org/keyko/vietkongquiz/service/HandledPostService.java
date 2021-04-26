package org.keyko.vietkongquiz.service;

import lombok.RequiredArgsConstructor;
import org.keyko.vietkongquiz.entity.HandledPost;
import org.keyko.vietkongquiz.repository.HandledPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HandledPostService {
    private final HandledPostRepository handledPostRepository;

    public List<HandledPost> findByGameType(String gameType) {
        return handledPostRepository.findByGameType(gameType);
    }

    public List<HandledPost> findByPostIdAndPostDateAndGameType(int postId, LocalDate postDate, String gameType) {
        return handledPostRepository.findByPostIdAndPostDateAndGameType(postId, postDate, gameType);
    }
}
