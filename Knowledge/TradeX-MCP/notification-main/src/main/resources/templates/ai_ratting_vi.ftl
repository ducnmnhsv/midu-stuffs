<OneSignalTitle>Cập Nhật Top 5 AI Rating ${date}</OneSignalTitle><#if ((codeIn?size > 0) || (codeOut?size > 0))>• Mua: <#list codeIn as x>${x}<#if x_has_next>, </#if></#list>
• Bán: <#list codeOut as x>${x}<#if x_has_next>, </#if></#list><#else>AI rating không có thay đổi cho hôm nay.</#if>
<#if (isNonLogin??)>Đăng ký tài khoản Paave để nhận được nhiều thông tin hữu ích hơn. Đăng ký ngay!</#if>