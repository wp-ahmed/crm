package site.easy.to.build.crm.customValidations;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FutureDateValidator implements ConstraintValidator<FutureDate, String> {
    @Override
    public void initialize(FutureDate constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String dateString, ConstraintValidatorContext context) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.isAfter(LocalDate.now()); // Check if the date is in the future
        } catch (DateTimeParseException e) {
            return false; // Invalid date format
        }
    }
}