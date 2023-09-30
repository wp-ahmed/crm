package site.easy.to.build.crm.customValidations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameDayValidator.class)
public @interface SameDay {
    String message() default "Start and end dates must be equal and on the same day";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}