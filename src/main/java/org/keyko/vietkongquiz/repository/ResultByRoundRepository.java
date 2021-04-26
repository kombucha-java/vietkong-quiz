package org.keyko.vietkongquiz.repository;

import org.keyko.vietkongquiz.entity.RoundResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultByRoundRepository extends CrudRepository<RoundResult, Long> {
}
