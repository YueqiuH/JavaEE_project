package com.smartcampus.common.aop;

//@Component
//@Aspect
public class AroundCut {
    // @Autowired
    // StringRedisTemplate redisTemplate;

    /*
    public static final String POINT_CUT = "execution(* com.smartcampus.*.controller.*.*(..))";

    @Around(AroundCut.POINT_CUT)
    public CommonResult checkToken(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(NoToken.class)) {
            return (CommonResult) pjp.proceed();
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Token");
        if (StringUtils.isBlank(token)) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        String userInfoString = redisTemplate.opsForValue().get("Token::" + token);
        if (StringUtils.isBlank(userInfoString)) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        return (CommonResult) pjp.proceed();
    }
    */
}
