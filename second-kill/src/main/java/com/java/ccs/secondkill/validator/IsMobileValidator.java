package com.java.ccs.secondkill.validator;

import com.java.ccs.secondkill.util.ValidatorUtil;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author caocs
 * @date 2021/10/22
 * <p>
 * 需要实现ConstraintValidator<注解，注解的定义>接口。
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        // 在初始化时，获取是否必填的值required。
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 然后，根据自定义的校验规则进行校验。
        if (required) {
            return ValidatorUtil.isMobile(value);
        }
        if (StringUtils.isEmpty(value)) {
            return true;
        } else {
            return ValidatorUtil.isMobile(value);
        }
    }
}
