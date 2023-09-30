package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser,Integer> {

    public OAuthUser findById(int id);

    public OAuthUser getOAuthUserByUser(User user);

    public OAuthUser findByEmail(String email);

    public void deleteById(int id);
}
