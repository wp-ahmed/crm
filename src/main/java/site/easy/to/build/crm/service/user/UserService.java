package site.easy.to.build.crm.service.user;

import site.easy.to.build.crm.entity.User;

import java.util.List;

public interface UserService {

    public long countAllUsers();

    public User findById(int id);

    public List<User> findByUsername(String username);

    public User findByEmail(String email);

    public User findByToken(String token);

    public User save(User user);

    public void deleteById(int id);

    public List<User> findAll();

}
