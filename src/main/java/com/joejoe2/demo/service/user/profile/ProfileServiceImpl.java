package com.joejoe2.demo.service.user.profile;

import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserProfile getProfile(String userId) throws UserDoesNotExist {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        return new UserProfile(user);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userRepository.findAll().stream().sorted(Comparator.comparing(User::getCreateAt)).map((UserProfile::new)).collect(Collectors.toList());
    }

    @Override
    public PageList<UserProfile> getAllUserProfilesWithPage(int page, int size) {
        if (page < 0 || size <= 0) throw new IllegalArgumentException("invalid page or size !");
        Page<User> paging = userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createAt")));
        List<UserProfile> profiles = paging.getContent().stream().map((UserProfile::new)).collect(Collectors.toList());
        return new PageList<>(paging.getTotalElements(), paging.getNumber(), paging.getTotalPages(), paging.getSize(), profiles);
    }
}
