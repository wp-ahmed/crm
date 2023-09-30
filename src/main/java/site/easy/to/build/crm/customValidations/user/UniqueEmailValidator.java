package site.easy.to.build.crm.customValidations.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserService userService;

    @Autowired
    public UniqueEmailValidator(UserService userService) {
        this.userService = userService;
    }
    public UniqueEmailValidator() {
        this.userService = null;
    }
    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if(email== null || email.isEmpty() || userService == null) {
            return true;
        }
        User existedUser = userService.findByEmail(email);
        return existedUser == null;
    }
}
