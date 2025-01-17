package com.prgrms.board.service;

import com.prgrms.board.domain.Member;
import com.prgrms.board.domain.Post;
import com.prgrms.board.dto.request.PostCreateDto;
import com.prgrms.board.dto.request.PostUpdateDto;
import com.prgrms.board.dto.response.PostResponseDto;
import com.prgrms.board.exception.EntityNotFoundException;
import com.prgrms.board.exception.IllegalModifyException;
import com.prgrms.board.repository.MemberRepository;
import com.prgrms.board.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

import static com.prgrms.board.service.PostServiceImpl.SESSION_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    HttpSession session;

    private Member savedMember;
    private Post savedPost;

    @BeforeEach
    void setup() {
        Member member = Member.builder()
                .name("기서")
                .age(26)
                .hobby("농구")
                .build();

        savedMember = memberRepository.save(member);

        Post post = Post.builder()
                .title("title")
                .content("this is content")
                .writer(member)
                .build();

        savedPost = postRepository.save(post);
    }

    @Test
    @DisplayName("저장되지 않은 회원이 게시글을 저장할 경우 예외가 발생한다.")
    void 게시글_저장_실패() {
        //given
        Long unknownId = 100L;
        PostCreateDto createDto = PostCreateDto.builder()
                .title("title1")
                .content("content1")
                .writerId(unknownId)
                .build();

        //when & then
        assertThrows(EntityNotFoundException.class, () -> postService.register(createDto));
    }

    @Test
    @DisplayName("게시글을 단건 조회할 때 Member의 정보와 함께 조회할 수 있다.")
    void 게시글_조회_성공() {
        //given & when
        PostResponseDto postResponseDto = postService.findById(savedPost.getId());

        //then
        assertThat(postResponseDto)
                .hasFieldOrPropertyWithValue("title", savedPost.getTitle())
                .hasFieldOrPropertyWithValue("content", savedPost.getContent())
                .hasFieldOrPropertyWithValue("writerId", savedMember.getId());
    }

    @Test
    @DisplayName("저장되지 않은 게시글 Id로 조회할 경우 예외가 발생한다.")
    void 게시글_조회_실패() {
        //given
        Long unknownId = 100L;

        //when & then
        assertThrows(EntityNotFoundException.class, () -> postService.findById(unknownId));
    }


    @Test
    @DisplayName("게시글의 정보를 수정할 수 있다.")
    void 게시글_수정_성공() {
        //given
        String updatedTitle = "update title";
        String updatedContent = "update content";

        Long postId = savedPost.getId();
        PostUpdateDto updateDto = PostUpdateDto.builder()
                .title(updatedTitle)
                .content(updatedContent)
                .writerId(savedMember.getId())
                .build();

        //when
        Long updatedPostId = postService.update(postId, updateDto);
        PostResponseDto responseDto = postService.findById(updatedPostId);

        //then
        assertThat(responseDto)
                .hasFieldOrPropertyWithValue("title", updatedTitle)
                .hasFieldOrPropertyWithValue("content", updatedContent);
    }

    @Test
    @DisplayName("수정 요청을 보낸 작성자와 기존 작성자가 다른 경우 예외가 발생한다.")
    void 게시글_수정_실패() {
        //given
        String updatedTitle = "update title";
        String updatedContent = "update content";
        Long unknownMemberId = 100L;
        Long postId = savedPost.getId();

        PostUpdateDto updateDto = PostUpdateDto.builder()
                .title(updatedTitle)
                .content(updatedContent)
                .writerId(unknownMemberId)
                .build();

        //when & then
        assertThrows(IllegalModifyException.class,
                () -> postService.update(postId, updateDto));

    }

    @Test
    @DisplayName("게시글의 CreatedBy는 세션에 저장된 Member로 자동 저장된다.")
    void 게시글_CreatedBy() {
        //given
        PostCreateDto createDto = PostCreateDto.builder().writerId(savedMember.getId())
                .title("title!")
                .content("content!")
                .build();

        //when
        Long postId = postService.register(createDto);
        Member sessionMember = (Member) session.getAttribute(SESSION_MEMBER);


        //then
        assertThat(sessionMember)
                .hasFieldOrPropertyWithValue("name", savedMember.getName())
                .hasFieldOrPropertyWithValue("age", savedMember.getAge())
                .hasFieldOrPropertyWithValue("hobby", savedMember.getHobby());

        PostResponseDto responseDto = postService.findById(postId);
        assertThat(responseDto.getWriterId()).isEqualTo(sessionMember.getId());
    }

}