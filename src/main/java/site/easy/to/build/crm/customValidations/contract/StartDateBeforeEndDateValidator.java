package site.easy.to.build.crm.customValidations.contract;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import site.easy.to.build.crm.entity.Contract;

import java.time.LocalDate;

public class StartDateBeforeEndDateValidator implements ConstraintValidator<StartDateBeforeEndDate, Contract> {

    @Override
    public void initialize(StartDateBeforeEndDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(Contract contract, ConstraintValidatorContext context) {
        if (contract == null ||contract.getStartDate() == null || contract.getEndDate() == null
                || contract.getStartDate().isEmpty() || contract.getEndDate().isEmpty()) {
            return true;
        }

        LocalDate startDate = LocalDate.parse(contract.getStartDate());
        LocalDate endDate = LocalDate.parse(contract.getEndDate());

        return startDate.isBefore(endDate);
    }
}