package com.smartcampus.auth.permission;

import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void requirePermission(RequirePermission requirePermission) {
        if (!CurrentUserContext.require().hasPermission(requirePermission.value())) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
    }
}
