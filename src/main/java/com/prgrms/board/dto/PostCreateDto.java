package com.prgrms.board.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class PostCreateDto {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private Long writerId;
}