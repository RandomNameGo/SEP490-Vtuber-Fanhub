package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.Badge;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBadge;
import com.sep490.vtuber_fanhub.repositories.BadgeRepository;
import com.sep490.vtuber_fanhub.repositories.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void awardBadge(User user, Long badgeId) {
        if (hasBadge(user, badgeId)) {
            return;
        }

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge with ID " + badgeId + " not found"));

        saveUserBadge(user, badge);
    }

    @Override
    @Transactional
    public void evaluateAndAward(User user, String type, Long currentValue) {
        List<Badge> eligibleBadges;
        if ("LIKE".equalsIgnoreCase(type)) {
            eligibleBadges = badgeRepository.findByTypeAndLikeRequireLessThanEqual("LIKE", currentValue);
        } else if ("COMMENT".equalsIgnoreCase(type)) {
            eligibleBadges = badgeRepository.findByTypeAndCommentRequireLessThanEqual("COMMENT", currentValue);
        } else if ("REGISTRATION".equalsIgnoreCase(type)) {
            eligibleBadges = badgeRepository.findByType("REGISTRATION");
        } else {
            return;
        }

        if (eligibleBadges.isEmpty()) {
            return;
        }

        java.util.Set<Long> ownedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(user.getId());

        for (Badge badge : eligibleBadges) {
            if (!ownedBadgeIds.contains(badge.getId())) {
                saveUserBadge(user, badge);
                sendBadgeNotification(user, badge);
            }
        }
    }

    private void saveUserBadge(User user, Badge badge) {
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setAcquiredAt(Instant.now());
        userBadge.setIsDisplay(false);

        userBadgeRepository.save(userBadge);
    }

    private void sendBadgeNotification(User user, Badge badge) {
        String title = "New Badge Unlocked! 🏅";
        String message = String.format("Congratulations! You've earned the \"%s\" badge.", badge.getBadgeName());
        notificationService.createNotification(user, "BADGE_AWARDED", title, message, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBadge(User user, Long badgeId) {
        java.util.Set<Long> ownedBadgeIds = userBadgeRepository.findBadgeIdsByUserId(user.getId());
        return ownedBadgeIds.contains(badgeId);
    }
}
