package com.prgrms.board.domain;

import com.prgrms.board.dto.request.PostUpdateDto;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "{exception.post.title.null}")
    private String title;

    @Lob
    @NotBlank(message = "{exception.post.content.null}")
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    //연관관계 편의 메소드
    public void registerMember(Member member) {
        if (Objects.nonNull(this.writer)) {
            this.writer.getPosts().remove(this);
        }

        this.writer = member;
        member.getPosts().add(this);
    }

    public void changePost(PostUpdateDto updateDto) {
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
    }
}
