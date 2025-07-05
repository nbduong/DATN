package com.zawser.datn.controller;

import com.zawser.datn.dto.request.ApiResponse;
import com.zawser.datn.dto.request.CommentRequest;
import com.zawser.datn.dto.response.CommentResponse;
import com.zawser.datn.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(request))
                .build();
    }

    @GetMapping(path = "/{productId}")
    public ApiResponse<List<CommentResponse>> getAllComments(@PathVariable String productId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .result(commentService.getCommentsByProductId(productId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable String id) {
        commentService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
