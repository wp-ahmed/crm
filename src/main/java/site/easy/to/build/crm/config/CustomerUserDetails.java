package site.easy.to.build.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.repository.CustomerLoginInfoRepository;
import site.easy.to.build.crm.service.role.RoleService;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerUserDetails implements UserDetailsService {

    @Autowired
    CustomerLoginInfoRepository customerLoginInfoRepository;
    @Autowired
    RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        CustomerLoginInfo customer = customerLoginInfoRepository.findByUsername(email);
        Role role = roleService.findByName("ROLE_CUSTOMER");

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getName()));
        if (customer == null) {
            throw new UsernameNotFoundException("Customer not found with email: " + email);
        }

        // Create and return a UserDetails object based on the retrieved customer
        return new org.springframework.security.core.userdetails.User(
                customer.getEmail(),
                customer.getPassword(),
                authorities
        );
    }
}
