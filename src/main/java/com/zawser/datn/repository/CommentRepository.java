package com.zawser.datn.repository;

import com.zawser.datn.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findALlByProductId(String productId);
}
