package site.easy.to.build.crm.service.user;

import site.easy.to.build.crm.entity.UserProfile;

import java.util.List;
import java.util.Optional;

public interface UserProfileService {

    public Optional<UserProfile> findById(int id);

    public UserProfile save(UserProfile userProfile);

    public UserProfile findByUserId(int userId);

    public List<UserProfile> getAllProfiles();
}
