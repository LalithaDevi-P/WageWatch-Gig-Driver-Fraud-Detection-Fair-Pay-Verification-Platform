package com.wagewatch.analytics.repository;

import com.wagewatch.analytics.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, String> {
    // We don't need any custom math queries here. Spring gives us basic find/save for free!
}