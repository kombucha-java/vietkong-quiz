package org.keyko.vietkongquiz.repository;

import org.keyko.vietkongquiz.entity.QuizGame;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizGameRepository extends CrudRepository<QuizGame, Long> {
}
