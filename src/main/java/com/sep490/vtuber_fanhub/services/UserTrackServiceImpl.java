package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserTrack;
import com.sep490.vtuber_fanhub.repositories.UserTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTrackServiceImpl implements UserTrackService {

    private final UserTrackRepository userTrackRepository;
    private final UserBadgeService userBadgeService;

    @Override
    @Transactional
    public void updateOnLike(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());

        Long newMaxLikes;
        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            Long currentMaxLikes = track.getMaxLikes() != null ? track.getMaxLikes() : 0L;
            newMaxLikes = currentMaxLikes + 1;
            track.setMaxLikes(newMaxLikes);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(1L);
            track.setMaxComments(0L);
            userTrackRepository.save(track);
            newMaxLikes = 1L;
        }

        // Award badges based on like requirements
        userBadgeService.evaluateAndAward(user, "LIKE", newMaxLikes);
    }

    @Override
    @Transactional
    public void updateOnComment(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());
        Long newMaxComments;

        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            newMaxComments = (track.getMaxComments() != null ? track.getMaxComments() : 0L) + 1;
            track.setMaxComments(newMaxComments);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(0L);
            track.setMaxComments(1L);
            userTrackRepository.save(track);
            newMaxComments = 1L;
        }

        // Award badges based on comment requirements
        userBadgeService.evaluateAndAward(user, "COMMENT", newMaxComments);
    }
}
