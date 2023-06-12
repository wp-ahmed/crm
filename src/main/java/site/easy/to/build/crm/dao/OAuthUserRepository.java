package site.easy.to.build.crm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.OAuthUser;

@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser,Integer> {

    public OAuthUser findById(int id);

    public void deleteById(int id);
}
