package com.intermediate.Blog.Application.Configurations;

import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Models.FollowRelation;
import com.intermediate.Blog.Application.Models.Post;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Models.UserProfile;
import com.intermediate.Blog.Application.Repositories.CategoryRepository;
import com.intermediate.Blog.Application.Repositories.FollowerRepository;
import com.intermediate.Blog.Application.Repositories.ProfileRepository;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Seeds 2000 test users with posts and follow relations when app.seed-test-users=true.
 * Users: testuser_1 .. testuser_2000, password "Test123!".
 * Each user gets 2-3 posts (with placeholder images and content) and follows ~40 others for testable lists.
 * Disable after first run (set app.seed-test-users=false).
 */
@Configuration
public class TestDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(TestDataSeeder.class);
    private static final int TOTAL_USERS = 2000;
    private static final int BATCH_SIZE = 200;
    private static final int POSTS_PER_USER_MIN = 2;
    private static final int POSTS_PER_USER_MAX = 3;
    private static final int FOLLOWS_PER_USER = 40;
    private static final String PASSWORD = "Test123!";
    private static final String USERNAME_PREFIX = "testuser_";
    private static final String EMAIL_DOMAIN = "@test.goconnect.local";

    private static final String[] POST_TITLES = {
        "Getting started with the new features",
        "Thoughts on technology and daily life",
        "A quick update from my side",
        "Something I wanted to share",
        "Ideas worth trying",
        "Lessons learned this week",
        "Building something new",
        "Reflections and next steps"
    };

    private static final String[] POST_CONTENTS = {
        "Hello everyone! Here's a short post to share some thoughts. Hope you're having a great day. Feel free to drop a comment below if you have any questions or want to connect.",
        "Just wrapping up a busy week. Wanted to put this out there for anyone interested. Looking forward to hearing your take on this topic.",
        "Quick post today. Sometimes the best ideas come when we least expect them. What do you think? Let me know in the comments.",
        "Sharing a few updates and some content that might be useful. Thanks for reading and see you in the next one!",
        "Here's something I've been working on. It's still early days but I'm excited about the direction. More to come soon."
    };

    private static final String[] CATEGORY_NAMES = { "Technology", "Lifestyle", "Travel", "General" };
    private static final String PLACEHOLDER_IMAGE_BASE = "https://picsum.photos/seed/";

    @Bean
    @ConditionalOnProperty(name = "app.seed-test-users", havingValue = "true")
    CommandLineRunner seedTestUsers(
            UserRepo userRepo,
            ProfileRepository profileRepository,
            CategoryRepository categoryRepository,
            PostRepository postRepository,
            FollowerRepository followerRepository,
            PasswordEncoder passwordEncoder) {
        return args -> runSeed(userRepo, profileRepository, categoryRepository,
                postRepository, followerRepository, passwordEncoder);
    }

    @Transactional
    protected void runSeed(UserRepo userRepo, ProfileRepository profileRepository,
                           CategoryRepository categoryRepository, PostRepository postRepository,
                           FollowerRepository followerRepository, PasswordEncoder passwordEncoder) {
        List<User> testUsers = userRepo.findAll().stream()
                .filter(u -> u.getUsername() != null && u.getUsername().startsWith(USERNAME_PREFIX))
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());

        boolean usersExisted = !testUsers.isEmpty();
        if (!usersExisted) {
            String encodedPassword = passwordEncoder.encode(PASSWORD);
            log.info("Test data seeder: creating {} test users (batch size {})...", TOTAL_USERS, BATCH_SIZE);
            List<User> batch = new ArrayList<>();
            for (int i = 1; i <= TOTAL_USERS; i++) {
                User user = new User();
                user.setUsername(USERNAME_PREFIX + i);
                user.setEmail(USERNAME_PREFIX + i + EMAIL_DOMAIN);
                user.setPassword(encodedPassword);
                user.setRole("USER");
                user.setVerified(true);
                batch.add(user);

                if (batch.size() >= BATCH_SIZE) {
                    List<User> saved = userRepo.saveAll(batch);
                    for (User u : saved) {
                        UserProfile profile = new UserProfile();
                        profile.setUser(u);
                        profileRepository.save(profile);
                    }
                    batch.clear();
                    log.info("Test data seeder: saved {} users so far.", Math.min(i, TOTAL_USERS));
                }
            }
            if (!batch.isEmpty()) {
                List<User> saved = userRepo.saveAll(batch);
                for (User u : saved) {
                    UserProfile profile = new UserProfile();
                    profile.setUser(u);
                    profileRepository.save(profile);
                }
            }
            testUsers = userRepo.findAll().stream()
                    .filter(u -> u.getUsername() != null && u.getUsername().startsWith(USERNAME_PREFIX))
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .collect(Collectors.toList());
        } else {
            log.info("Test data seeder: {} test users already exist, seeding posts & follows only.", testUsers.size());
        }

        ensureCategories(categoryRepository);
        List<Category> categories = categoryRepository.findAll();
        if (testUsers.isEmpty()) {
            log.info("Test data seeder: no test users, nothing more to do.");
            return;
        }
        User firstTestUser = testUsers.get(0);
        boolean postsExist = postRepository.countByUser(firstTestUser) >= POSTS_PER_USER_MIN;
        boolean followsExist = followerRepository.findByFollower(firstTestUser).size() >= FOLLOWS_PER_USER;

        if (categories.isEmpty()) {
            log.warn("Test data seeder: no categories found, skipping posts.");
        } else if (!postsExist) {
            seedPosts(testUsers, categories, postRepository);
        } else {
            log.info("Test data seeder: posts already exist, skipping.");
        }
        if (!followsExist) {
            seedFollows(testUsers, followerRepository);
        } else {
            log.info("Test data seeder: follow relations already exist, skipping.");
        }

        log.info("Test data seeder: finished. {} users, posts (2-3 per user), follows (~{} per user). Password: {}",
                testUsers.size(), FOLLOWS_PER_USER, PASSWORD);
    }

    private void ensureCategories(CategoryRepository categoryRepository) {
        if (categoryRepository.count() > 0) {
            return;
        }
        for (String name : CATEGORY_NAMES) {
            if (!categoryRepository.existsByName(name)) {
                Category c = new Category();
                c.setName(name);
                c.setDescription("Category: " + name);
                categoryRepository.save(c);
            }
        }
        log.info("Test data seeder: created default categories.");
    }

    private void seedPosts(List<User> testUsers, List<Category> categories, PostRepository postRepository) {
        int postsCreated = 0;
        int postBatchSize = 500;
        List<Post> postBatch = new ArrayList<>();

        for (int u = 0; u < testUsers.size(); u++) {
            User user = testUsers.get(u);
            int numPosts = POSTS_PER_USER_MIN + (u % (POSTS_PER_USER_MAX - POSTS_PER_USER_MIN + 1));

            for (int p = 0; p < numPosts; p++) {
                Post post = new Post();
                post.setUser(user);
                post.setTitle(POST_TITLES[(u + p) % POST_TITLES.length] + " – " + user.getUsername());
                post.setContent(POST_CONTENTS[(u + p) % POST_CONTENTS.length]);
                post.setCategory(categories.get((u + p) % categories.size()));
                post.setPostImage(PLACEHOLDER_IMAGE_BASE + "u" + user.getId() + "p" + p + "/800/500");
                postBatch.add(post);
                postsCreated++;
            }

            if (postBatch.size() >= postBatchSize) {
                postRepository.saveAll(postBatch);
                postBatch.clear();
                log.info("Test data seeder: saved {} posts so far.", postsCreated);
            }
        }
        if (!postBatch.isEmpty()) {
            postRepository.saveAll(postBatch);
        }
        log.info("Test data seeder: created {} posts.", postsCreated);
    }

    private void seedFollows(List<User> testUsers, FollowerRepository followerRepository) {
        if (testUsers.size() < 2) {
            return;
        }
        int n = testUsers.size();
        int followBatchSize = 1000;
        List<FollowRelation> followBatch = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < n; i++) {
            User follower = testUsers.get(i);
            for (int k = 1; k <= FOLLOWS_PER_USER; k++) {
                int j = (i + k) % n;
                if (i == j) continue;
                User following = testUsers.get(j);
                FollowRelation rel = new FollowRelation();
                rel.setFollower(follower);
                rel.setFollowing(following);
                rel.setCreatedAt(now);
                followBatch.add(rel);
            }
            if (followBatch.size() >= followBatchSize) {
                followerRepository.saveAll(followBatch);
                followBatch.clear();
            }
        }
        if (!followBatch.isEmpty()) {
            followerRepository.saveAll(followBatch);
        }
        log.info("Test data seeder: created follow relations (~{} per user).", FOLLOWS_PER_USER);
    }
}
