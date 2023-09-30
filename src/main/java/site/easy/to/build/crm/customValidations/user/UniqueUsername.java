package site.easy.to.build.crm.customValidations.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
    String message() default "The username you entered is already registered. Please choose a different username.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
