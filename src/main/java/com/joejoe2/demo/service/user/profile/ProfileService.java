package com.joejoe2.demo.service.user.profile;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.UserDoesNotExist;

import java.util.List;

public interface ProfileService {
    /**
     * load UserProfile from db with given userId
     * @param userId
     * @return
     * @throws UserDoesNotExist if target user is not exist
     */
    UserProfile getProfile(String userId) throws UserDoesNotExist;

    /**
     * get all user profiles from db
     * @return all user profiles
     */
    List<UserProfile> getAllUserProfiles();

    /**
     * get all user profiles from db with page request
     * @param page must>=0
     * @param size must>0
     * @return paged user profiles
     */
    PageList<UserProfile> getAllUserProfilesWithPage(int page, int size);
}
