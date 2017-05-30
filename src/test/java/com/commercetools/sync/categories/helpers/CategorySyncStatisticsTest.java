package com.commercetools.sync.categories.helpers;


import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CategorySyncStatisticsTest {
    private static final long ONE_HOUR_FIFTEEN_MINUTES_AND_TWENTY_SECONDS_IN_MILLIS = 75 * 60 * 1000 + 20 * 1000L;

    private CategorySyncStatisticsBuilder categorySyncStatisticsBuilder;

    @Before
    public void setup() {
        categorySyncStatisticsBuilder = new CategorySyncStatisticsBuilder();
    }

    @Test
    public void getUpdated_WithNoUpdated_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getUpdated()).isEqualTo(0);
    }

    @Test
    public void incrementUpdated_ShouldIncrementUpdatedValue() {
        categorySyncStatisticsBuilder.incrementUpdated();
        assertThat(categorySyncStatisticsBuilder.build().getUpdated()).isEqualTo(1);
    }

    @Test
    public void getCreated_WithNoCreated_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getCreated()).isEqualTo(0);
    }

    @Test
    public void incrementCreated_ShouldIncrementCreatedValue() {
        categorySyncStatisticsBuilder.incrementCreated();
        assertThat(categorySyncStatisticsBuilder.build().getCreated()).isEqualTo(1);
    }

    @Test
    public void getUpToDate_WithNoUpToDate_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getUpToDate()).isEqualTo(0);
    }

    @Test
    public void incrementUpToDate_ShouldIncrementUpToDateValue() {
        categorySyncStatisticsBuilder.incrementUpToDate();
        assertThat(categorySyncStatisticsBuilder.build().getUpToDate()).isEqualTo(1);
    }

    @Test
    public void getProcessed_WithNoProcessed_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getProcessed()).isEqualTo(0);
    }

    @Test
    public void getProcessed_WithOtherStatsIncremented_ShouldReturnSumOfOtherValues() {
        categorySyncStatisticsBuilder.incrementCreated();
        categorySyncStatisticsBuilder.incrementUpdated();
        categorySyncStatisticsBuilder.incrementUpToDate();
        categorySyncStatisticsBuilder.incrementFailed();
        assertThat(categorySyncStatisticsBuilder.build().getProcessed()).isEqualTo(4);
    }

    @Test
    public void getFailed_WithNoFailed_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getFailed()).isEqualTo(0);
    }

    @Test
    public void incrementFailed_ShouldIncrementFailedValue() {
        categorySyncStatisticsBuilder.incrementFailed();
        assertThat(categorySyncStatisticsBuilder.build().getFailed()).isEqualTo(1);
    }

    @Test
    public void getProcesingTimeInMillis_WithNoProcessingTime_ShouldReturnZero() {
        assertThat(categorySyncStatisticsBuilder.build().getProcessingTimeInMillis()).isEqualTo(0L);
    }

    @Test
    public void setProcesingTimeInMillis_ShouldSetProcessingTimeValue() {
        categorySyncStatisticsBuilder.setProcessingTimeInMillis(ONE_HOUR_FIFTEEN_MINUTES_AND_TWENTY_SECONDS_IN_MILLIS);
        assertThat(categorySyncStatisticsBuilder.build().getProcessingTimeInMillis())
            .isEqualTo(ONE_HOUR_FIFTEEN_MINUTES_AND_TWENTY_SECONDS_IN_MILLIS);
    }

    @Test
    public void getFormattedProcessingTime_ShouldReturnFormattedString() {
        categorySyncStatisticsBuilder.setProcessingTimeInMillis(ONE_HOUR_FIFTEEN_MINUTES_AND_TWENTY_SECONDS_IN_MILLIS);
        assertThat(categorySyncStatisticsBuilder.build().getFormattedProcessingTime("d'd, 'H'h, 'm'm, 's's, 'S'ms'"))
            .isEqualTo("0d, 1h, 15m, 20s, 000ms");
    }
}
