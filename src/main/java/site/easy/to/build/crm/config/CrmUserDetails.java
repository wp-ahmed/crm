package site.easy.to.build.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.dao.UserRepository;
import site.easy.to.build.crm.entity.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrmUserDetails implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String crmUsername, password = null;
        List<GrantedAuthority> authorities = null;
        List<User> user = userRepository.findByUsername(username);
        if(user.size() == 0) {
            throw new UsernameNotFoundException("user details not found for the user : " + username);
        } else {
            crmUsername = user.get(0).getUsername();
            password = user.get(0).getPassword();
            authorities = new ArrayList<>();
        }

        return new org.springframework.security.core.userdetails.User(username,password,authorities);
    }
}
