package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.models.User;

public interface UserBadgeService {

    void awardBadge(User user, Long badgeId);

    boolean hasBadge(User user, Long badgeId);
}
