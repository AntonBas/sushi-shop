package com.sushishop.review;

import com.sushishop.shared.BaseEntity;
import com.sushishop.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review_replies", indexes = {
        @Index(name = "idx_review_replies_review_id", columnList = "review_id"),
        @Index(name = "idx_review_replies_user_id", columnList = "user_id")
})
@EqualsAndHashCode(callSuper = true, exclude = {"review", "user"})
public class ReviewReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 250)
    @Column(nullable = false, length = 250)
    private String message;
}
