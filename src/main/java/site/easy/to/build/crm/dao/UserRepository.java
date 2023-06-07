package site.easy.to.build.crm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    public User findById(int id);

    public List<User> findByUsername(String username);

    public User findByEmail(String email);

    public void deleteById(int id);
}
