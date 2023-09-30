package site.easy.to.build.crm.customValidations.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

import java.util.List;

public class UniqueUsernameValidator  implements ConstraintValidator<UniqueUsername, String> {

    private final UserService userService;

    @Autowired
    public UniqueUsernameValidator(UserService userService) {
        this.userService = userService;
    }

    public UniqueUsernameValidator() {
        this.userService = null;
    }

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        if(username== null || username.isEmpty() || userService == null) {
            return true;
        }
        List<User> existedUser = userService.findByUsername(username);
        return existedUser == null || existedUser.isEmpty();
    }
}
