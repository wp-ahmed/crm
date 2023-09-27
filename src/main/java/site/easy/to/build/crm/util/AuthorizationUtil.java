package site.easy.to.build.crm.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import site.easy.to.build.crm.entity.User;

import java.util.Objects;

public class  AuthorizationUtil {

    public static Boolean  checkIfUserAuthorized(User employee, User loggedinUser) {
        return Objects.equals(loggedinUser.getId(), employee.getId());
    }

    public static Boolean hasRole(Authentication authentication, String role) {
        GrantedAuthority authorityToCheck = new SimpleGrantedAuthority(role);
        return authentication.getAuthorities().contains(authorityToCheck);
    }
}
