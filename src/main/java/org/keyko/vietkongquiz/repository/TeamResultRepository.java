package org.keyko.vietkongquiz.repository;

import org.keyko.vietkongquiz.entity.TeamResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamResultRepository extends CrudRepository<TeamResult, Long> {
}
