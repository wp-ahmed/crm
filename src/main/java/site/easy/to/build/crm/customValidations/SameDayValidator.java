package site.easy.to.build.crm.customValidations;

import site.easy.to.build.crm.google.model.calendar.EventDisplay;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class SameDayValidator implements ConstraintValidator<SameDay, EventDisplay> {
    @Override
    public void initialize(SameDay constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(EventDisplay eventDisplay, ConstraintValidatorContext context) {
        if (eventDisplay == null) {
            return true; // Skip validation if object is null
        }
        if(eventDisplay.getStartDate() == null || eventDisplay.getStartDate().isEmpty()){
            return true;
        }
        if(eventDisplay.getEndDate() == null || eventDisplay.getEndDate().isEmpty()){
            return true;
        }
        LocalDate startDate = LocalDate.parse(eventDisplay.getStartDate());
        LocalDate endDate = LocalDate.parse(eventDisplay.getEndDate());

        return startDate.isEqual(endDate);
    }
}