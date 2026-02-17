package com.milind.docintel.repository;

import com.milind.docintel.entity.AiResponseSource;
import com.milind.docintel.entity.AiResponseSourceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResponseSourceRepository extends JpaRepository<AiResponseSource, AiResponseSourceId> {
}
