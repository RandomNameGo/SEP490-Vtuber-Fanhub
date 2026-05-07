package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDailyMissionServiceImpl implements UserDailyMissionService {


    private final UserDailyMissionRepository userDailyMissionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void resetDailyMission() {

        List<UserDailyMission> userDailyMissions = userDailyMissionRepository.findAll();

        for (UserDailyMission userDailyMission : userDailyMissions) {
            userDailyMission.setLikeAmount(0);
            userDailyMission.setCommentAmount(0);
            userDailyMission.setBonus10(false);
            userDailyMission.setBonus20(false);
            userDailyMissionRepository.save(userDailyMission);
        }

    }

    @Override
    @Transactional
    public void awardPoints(Long userId, String actionType) {
        UserDailyMission mission = userDailyMissionRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new RuntimeException("User daily mission not found"));

        int currentAmount = 0;
        String milestoneName = "";

        if ("LIKE".equals(actionType)) {
            mission.setLikeAmount(mission.getLikeAmount() + 1);
            currentAmount = mission.getLikeAmount();
            milestoneName = "5 Likes Milestone";

            // Award 5 points every 5 likes, capped at 50 points (50 likes)
            if (currentAmount > 0 && currentAmount % 5 == 0 && currentAmount <= 50) {
                User user = mission.getUser();
                user.setPoints((user.getPoints() != null ? user.getPoints() : 0) + 5);
                userRepository.save(user);
                notificationService.sendDailyMissionPointsNotification(userId, 5, milestoneName);
            }

            // Still use awardPointsForLikes for daily mission bonus (10, 20 milestones)
            this.awardPointsForLikes(userId, currentAmount);
        } else if ("COMMENT".equals(actionType)) {
            int commentAmount = mission.getCommentAmount() != null ? mission.getCommentAmount() : 0;
            mission.setCommentAmount(commentAmount + 1);
            currentAmount = mission.getCommentAmount();
            milestoneName = "5 Comments Milestone";

            // Award 5 points every 5 comments, capped at 50 points (50 comments)
            if (currentAmount > 0 && currentAmount % 5 == 0 && currentAmount <= 50) {
                User user = mission.getUser();
                user.setPoints((user.getPoints() != null ? user.getPoints() : 0) + 5);
                userRepository.save(user);
                notificationService.sendDailyMissionPointsNotification(userId, 5, milestoneName);
            }
        }

        userDailyMissionRepository.save(mission);
    }

    @Override
    @Transactional
    public void awardPointsForLikes(Long userId, Integer likeAmount) {
        UserDailyMission mission = userDailyMissionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User daily mission not found"));

        if (likeAmount >= 10 && !Boolean.TRUE.equals(mission.getBonus10())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPoints(user.getPoints() + 20);
            userRepository.save(user);
            mission.setBonus10(true);
            userDailyMissionRepository.save(mission);
            notificationService.sendDailyMissionPointsNotification(userId, 20, "10 Likes Milestone");
        }

        if (likeAmount >= 20 && !Boolean.TRUE.equals(mission.getBonus20())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPoints(user.getPoints() + 40);
            userRepository.save(user);
            mission.setBonus20(true);
            userDailyMissionRepository.save(mission);
            notificationService.sendDailyMissionPointsNotification(userId, 40, "20 Likes Milestone");
        }
    }
}
