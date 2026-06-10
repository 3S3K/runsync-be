package com._s3k.runsync.domain.run.repository;

import java.math.BigDecimal;

public interface MonthlyStatsProjection {
    BigDecimal getTotalDistance();
    Long getTotalRunCount();
    Long getTotalDurationSeconds();
}
