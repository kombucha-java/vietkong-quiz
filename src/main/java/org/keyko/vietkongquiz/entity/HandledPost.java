package org.keyko.vietkongquiz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "handled_posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HandledPost {
    @Id
    @Column(name = "handled_post_id", updatable = false)
    @SequenceGenerator(
            name="handled_posts_handled_post_id_seq",
            sequenceName="handled_posts_handled_post_id_seq",
            allocationSize=1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator="handled_posts_handled_post_id_seq"
    )
    private Long handledPostId;

    @Column(name = "post_date")
    private LocalDate postDate;

    @Column(name = "post_id")
    private int postId;

    @Column(name = "game_type")
    private String gameType;
}
