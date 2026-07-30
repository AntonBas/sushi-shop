package com.sushishop.audit;

import com.sushishop.shared.enums.AuditAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String entityName;

    private Long entityId;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String details;

    @NotBlank
    @Column(nullable = false)
    private String performedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime performedAt;
}
