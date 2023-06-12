package site.easy.to.build.crm.service;

import site.easy.to.build.crm.entity.User;

import java.util.List;

public interface UserService {

    public User findById(int id);

    public List<User> findByUsername(String username);

    public User findByEmail(String email);

    public void save(User user);

    public void deleteById(int id);

}
