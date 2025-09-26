package com.example.community_blog.services;

import com.example.community_blog.dto.CreatePostRequest;
import com.example.community_blog.models.CommentModel;
import com.example.community_blog.models.PostModel;
import com.example.community_blog.models.UserModel;
import com.example.community_blog.repositories.PostRepository;
import com.example.community_blog.repositories.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${blog.page.size}")
    private int PAGE_SIZE;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostModel getPostById(Long id) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        return updatePostView(id);
    }

    private PostModel updatePostView(Long id) throws BadRequestException {
        PostModel post = postRepository.findById(id).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }
        // Increment view count via bulk update to avoid triggering @PreUpdate (and thus not touch updatedAt)
        postRepository.incrementViewCount(id);
        // Reflect the increment locally for the returned object without persisting it again
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    public PostModel publishPost(CreatePostRequest createPostRequest) throws BadRequestException {
        // Generate a post model and save to db
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = new PostModel();
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setAllowComment(createPostRequest.isAllowComment());
        post.setTags(createPostRequest.getTags());
        post.setAuthor(currentUser);
        return postRepository.save(post);
    }

    public Page<PostModel> getLatestPosts() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);
        return postRepository.findAll(pageable);
    }

    public Page<PostModel> getNotablePostsExceptFor(Long postId) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        return postRepository.findNotable(postId, pageable);
    }

    public Page<PostModel> searchRelatedPostsByTag(Long id, Set<String> tags) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created_at");
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);
        // Convert from a set to a string array
        return postRepository.searchPostsByTagsFuzzy(id, tags.toArray(new String[]{}), pageable);
    }

    public void addCommentToPost(Long id, String content) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = postRepository.findById(id).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }

        CommentModel commentModel = new CommentModel();
        commentModel.setCommenter(currentUser);
        commentModel.setContent(content);
        post.addComment(commentModel);
        postRepository.save(post);
    }

    public void likePost(Long postId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }

        post.addUserWhoLikePost(currentUser);
        postRepository.save(post);
    }

    public void dislikePost(Long postId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }

        post.addUserWhoDislikePost(currentUser);
        postRepository.save(post);
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
