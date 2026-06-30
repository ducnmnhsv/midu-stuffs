<OneSignalTitle>Top-5 AI Rating Update ${date}</OneSignalTitle><#if ((codeIn?size > 0) || (codeOut?size > 0))>• Buy: <#list codeIn as x>${x}<#if x_has_next>, </#if></#list>
• Sell: <#list codeOut as x>${x}<#if x_has_next>, </#if></#list><#else>No AI RATING change for today.</#if>
<#if (isNonLogin??)>Sign up for a Paave account to receive more important information! Register now!</#if>