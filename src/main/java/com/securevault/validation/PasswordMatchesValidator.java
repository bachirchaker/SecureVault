package com.securevault.validation;

import com.securevault.dto.ChangePasswordDto;
import com.securevault.dto.UserRegistrationDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof UserRegistrationDto regDto) {
            boolean matches = regDto.getPassword() != null && regDto.getPassword().equals(regDto.getConfirmPassword());
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("confirmPassword")
                        .addConstraintViolation();
            }
            return matches;
        } else if (obj instanceof ChangePasswordDto changeDto) {
            boolean matches = changeDto.getNewPassword() != null && changeDto.getNewPassword().equals(changeDto.getConfirmNewPassword());
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("confirmNewPassword")
                        .addConstraintViolation();
            }
            return matches;
        }
        return true;
    }
}
