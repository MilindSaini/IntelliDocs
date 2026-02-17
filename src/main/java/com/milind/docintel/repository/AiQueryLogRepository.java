package com.milind.docintel.repository;

import com.milind.docintel.entity.AiQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, UUID> {
}
