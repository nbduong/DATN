package com.zawser.datn.service;

import com.zawser.datn.dto.request.CommentRequest;
import com.zawser.datn.dto.response.CommentResponse;
import com.zawser.datn.entity.Comment;
import com.zawser.datn.entity.User;
import com.zawser.datn.mapper.CommentMapper;
import com.zawser.datn.repository.CommentRepository;
import com.zawser.datn.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {

    CommentMapper commentMapper;
    CommentRepository commentRepository;
    UserRepository userRepository;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('STORAGE_MANAGER') or hasRole('USER_MANAGER')")
    public CommentResponse createComment(CommentRequest request){
        Comment comment = commentMapper.toComment(request);
        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        comment.setCreatedBy(user.getUsername());
        comment.setCreatedDate(LocalDateTime.now());
        comment.setLastModifiedBy(user.getUsername());
        comment.setLastModifiedDate(LocalDateTime.now());
        commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('STORAGE_MANAGER') or hasRole('USER_MANAGER')")
    public CommentResponse delete(String id){
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
        return commentMapper.toResponse(comment);
    }

    public List<CommentResponse> getCommentsByProductId(String productId) {
        return commentRepository.findALlByProductId(productId).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
