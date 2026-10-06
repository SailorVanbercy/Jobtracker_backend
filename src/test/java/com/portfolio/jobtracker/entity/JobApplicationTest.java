package com.portfolio.jobtracker.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

/**
 * Guards the JPA mapping of the audit/description columns (no database needed).
 */
class JobApplicationTest {

    @Test
    void createdAt_isAnImmutableCreationTimestamp() throws NoSuchFieldException {
        Field createdAt = JobApplication.class.getDeclaredField("createdAt");

        assertThat(createdAt.getType()).isEqualTo(Instant.class);
        assertThat(createdAt.isAnnotationPresent(CreationTimestamp.class)).isTrue();
        Column column = createdAt.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.updatable()).isFalse();
    }

    @Test
    void createdAt_hasDatabaseDefault_soSchemaUpdateSucceedsOnExistingRows() throws NoSuchFieldException {
        Field createdAt = JobApplication.class.getDeclaredField("createdAt");

        ColumnDefault columnDefault = createdAt.getAnnotation(ColumnDefault.class);
        assertThat(columnDefault).isNotNull();
        assertThat(columnDefault.value()).isEqualTo("CURRENT_TIMESTAMP");
    }

    @Test
    void jobDescription_isMappedAsTextColumn() throws NoSuchFieldException {
        Field jobDescription = JobApplication.class.getDeclaredField("jobDescription");

        assertThat(jobDescription.getType()).isEqualTo(String.class);
        Column column = jobDescription.getAnnotation(Column.class);
        assertThat(column).isNotNull();
        assertThat(column.columnDefinition()).isEqualTo("TEXT");
    }
}
