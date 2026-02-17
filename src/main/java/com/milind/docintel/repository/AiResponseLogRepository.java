package com.milind.docintel.repository;

import com.milind.docintel.entity.AiResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiResponseLogRepository extends JpaRepository<AiResponseLog, UUID> {
}
