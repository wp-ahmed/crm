package site.easy.to.build.crm.customValidations.contract;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartDateBeforeEndDateValidator.class)
public @interface StartDateBeforeEndDate {
    String message() default "Start date must be before end date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}