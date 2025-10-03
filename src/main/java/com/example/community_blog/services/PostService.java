package com.example.community_blog.services;

import com.example.community_blog.dto.BlogPublishedEvent;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${blog.page.size}")
    private int PAGE_SIZE;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
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
        PostModel saved = postRepository.save(post);

        BlogPublishedEvent event = new BlogPublishedEvent(saved.getId(), currentUser.getId(), saved.getTitle());
        redisTemplate.convertAndSend("blog-published", event);

        return saved;
    }

    public Page<PostModel> getLatestPosts() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);
        return postRepository.findAll(pageable);
    }

    public Page<PostModel> getPosts(int page, String query, String sortBy, String sortDir) {
        if (page < 0) {
            page = 0;
        }

        Pageable pageable;
        // Sorting by createdAt and highPoint (most popular)
        // Query by tags, title and author's name

        if ("createdAt".equalsIgnoreCase(sortBy)) {
            Sort sort;
            sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by("createdAt").ascending()
                    : Sort.by("createdAt").descending();

            pageable = PageRequest.of(page, PAGE_SIZE, sort);

            return postRepository.searchPostsByTitleOrTagsOrAuthorOrderByCreatedAt(query, pageable);
        } else if ("highPoint".equalsIgnoreCase(sortBy)) {
            pageable = PageRequest.of(page, PAGE_SIZE);

            if (sortDir.equalsIgnoreCase("asc")) {
                return postRepository.searchPostsByTitleOrTagsOrAuthorOrderByHighPointAsc(query, pageable);
            } else {
                return postRepository.searchPostsByTitleOrTagsOrAuthorOrderByHighPointDesc(query, pageable);
            }
        } else {
            return null;
        }
    }

    public Page<PostModel> getNotablePostsExceptFor(Long postId) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        return postRepository.findNotable(postId, pageable);
    }

    public Page<PostModel> getUserNotablePosts(Long userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);
        return postRepository.findAllByAuthorId(userId, pageable);
    }

    public Page<PostModel> searchRelatedPostsByTag(Long postId, Set<String> tags) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created_at");
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);
        // Convert from a set to a string array
        return postRepository.searchPostsByTagsFuzzy(postId, tags.toArray(new String[]{}), pageable);
    }

    public CommentModel addCommentToPost(Long id, String content) throws BadRequestException {
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
        PostModel postModel = postRepository.save(post);

        return postModel.getComments().getLast();
    }

    public PostModel likePost(Long postId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }

        post.addUserWhoLikePost(currentUser);
        return postRepository.save(post);
    }

    public PostModel dislikePost(Long postId) throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        PostModel post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new BadRequestException("Post not found");
        }

        post.addUserWhoDislikePost(currentUser);
        return postRepository.save(post);
    }

    private UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    public Long countPosts() {
        return postRepository.count();
    }

    public Long calculatePostsView() throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        return postRepository.sumViewCountByAuthorId(currentUser.getId());
    }

    public Long calculatePostsLike() throws BadRequestException {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("User not authenticated");
        }

        return postRepository.sumLikeCountByAuthorId(currentUser.getId());
    }
}
