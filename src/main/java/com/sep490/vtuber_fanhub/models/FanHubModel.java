package com.sep490.vtuber_fanhub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "fan_hub_model")
public class FanHubModel {
    @Id
    @Column(name = "model_id", nullable = false)
    private Long id;

    @Column(name = "name", length = 512)
    private String name;

    @Lob
    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "sprites")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> sprites;

}