package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.UserDailyMission;
import com.sep490.vtuber_fanhub.repositories.UserDailyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDailyMissionServiceImpl implements UserDailyMissionService {


    private final UserDailyMissionRepository userDailyMissionRepository;


    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyMission() {

        List<UserDailyMission> userDailyMissions = userDailyMissionRepository.findAll();

        for (UserDailyMission userDailyMission : userDailyMissions) {
            userDailyMission.setLikeAmount(0);
            userDailyMissionRepository.save(userDailyMission);
        }

    }
}
