package com.smartcampus.app.security;

/** 成员 C 协同办公域权限码，与数据库 permission 表保持一致。 */
public final class OfficePermissions {

    public static final String FEE_SELF_READ = "fee:self:read";
    public static final String FEE_SELF_PAY = "fee:self:pay";
    public static final String FEE_MANAGE = "fee:manage";
    public static final String FEE_OVERVIEW_READ = "fee:overview:read";
    public static final String ASSET_READ = "asset:read";
    public static final String ASSET_APPLY = "asset:apply";
    public static final String ASSET_MANAGE = "asset:manage";
    public static final String WORK_PLAN_SELF = "work-plan:self";
    public static final String WORK_PLAN_MANAGE = "work-plan:manage";
    public static final String DOCUMENT_SELF = "document:self";
    public static final String DOCUMENT_APPROVE = "document:approve";
    public static final String DOCUMENT_MANAGE = "document:manage";
    public static final String MEETING_SELF = "meeting:self";
    public static final String MEETING_MANAGE = "meeting:manage";
    public static final String NOTIFICATION_SELF_READ = "notification:self:read";
    private OfficePermissions() {
    }
}
