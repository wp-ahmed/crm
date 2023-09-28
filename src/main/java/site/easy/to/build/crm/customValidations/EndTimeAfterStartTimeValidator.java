package site.easy.to.build.crm.customValidations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import site.easy.to.build.crm.google.model.calendar.EventDisplay;

import java.time.LocalTime;

public class EndTimeAfterStartTimeValidator implements ConstraintValidator<EndTimeAfterStartTime, EventDisplay> {

    @Override
    public void initialize(EndTimeAfterStartTime constraintAnnotation) {
    }

    @Override
    public boolean isValid(EventDisplay event, ConstraintValidatorContext context) {
        if (event == null) {
            return true; // null values are considered valid
        }
        if(event.getEndTime() == null || event.getEndTime().isEmpty() || event.getStartTime() == null || event.getStartTime().isEmpty()){
            return true;
        }
        LocalTime startTime = LocalTime.parse(event.getStartTime());
        LocalTime endTime = LocalTime.parse(event.getEndTime());
        return endTime.isAfter(startTime);
    }
}