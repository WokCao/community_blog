package com.example.community_blog.services;

import com.example.community_blog.models.CommentModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.CommentRepository;
import com.example.community_blog.repositories.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public Long likeComment(Long commentId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        CommentModel comment = commentRepository.findById(commentId).orElse(null);

        if (comment == null) {
            throw new BadRequestException("Post not found");
        }

        Long postId = comment.getPost().getId();

        comment.addUserWhoLikeComment(currentUser);
        commentRepository.save(comment);

        return postId;
    }

    public Long dislikeComment(Long commentId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        CommentModel comment = commentRepository.findById(commentId).orElse(null);

        if (comment == null) {
            throw new BadRequestException("Post not found");
        }

        Long postId = comment.getPost().getId();

        comment.addUserWhoDislikeComment(currentUser);
        commentRepository.save(comment);

        return postId;
    }

    private UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }
}
