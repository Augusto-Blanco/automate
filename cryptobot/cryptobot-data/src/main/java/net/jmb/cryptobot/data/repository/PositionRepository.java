package net.jmb.cryptobot.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.jmb.cryptobot.data.entity.Position;

@Repository
@Transactional
public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {



}
