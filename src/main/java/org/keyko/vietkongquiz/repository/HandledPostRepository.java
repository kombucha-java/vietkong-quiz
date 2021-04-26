package org.keyko.vietkongquiz.repository;

import org.keyko.vietkongquiz.entity.HandledPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HandledPostRepository extends CrudRepository<HandledPost, Long> {
    List<HandledPost> findByGameType(String gameType);

    List<HandledPost> findByPostIdAndPostDateAndGameType(int postId, LocalDate postDate, String gameType);
}
