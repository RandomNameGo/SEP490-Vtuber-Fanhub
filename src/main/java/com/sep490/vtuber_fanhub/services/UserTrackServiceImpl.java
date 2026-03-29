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

    @Override
    @Transactional
    public void updateOnLike(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());

        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            Long currentMaxLikes = track.getMaxLikes() != null ? track.getMaxLikes() : 0L;
            track.setMaxLikes(currentMaxLikes + 1);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(1L);
            track.setMaxComments(0L);
            userTrackRepository.save(track);
        }
    }

    @Override
    @Transactional
    public void updateOnComment(User user) {
        Optional<UserTrack> existingTrack = userTrackRepository.findByUserId(user.getId());

        if (existingTrack.isPresent()) {
            UserTrack track = existingTrack.get();
            Long currentMaxComments = track.getMaxComments() != null ? track.getMaxComments() : 0L;
            track.setMaxComments(currentMaxComments + 1);
            userTrackRepository.save(track);
        } else {
            UserTrack track = new UserTrack();
            track.setUser(user);
            track.setMaxLikes(0L);
            track.setMaxComments(1L);
            userTrackRepository.save(track);
        }
    }
}
