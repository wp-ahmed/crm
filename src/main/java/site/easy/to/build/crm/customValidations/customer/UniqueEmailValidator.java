package site.easy.to.build.crm.customValidations.customer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.service.customer.CustomerService;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail,String> {

    private final CustomerService customerService;

    @Autowired
    public UniqueEmailValidator(CustomerService customerService) {
        this.customerService = customerService;
    }

    public UniqueEmailValidator() {
        this.customerService = null;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if(customerService == null || email == null || email.isEmpty()){
            return true;
        }
        Customer customer = customerService.findByEmail(email);
        return customer == null;
    }
}
