<#if isCeilPrice>
    <#assign imagePath = "▲">
    <#assign notiContent = "${symbol} has reached Ceiling Price : ${priceInfo}">
<#else>
    <#assign imagePath = "▼">
    <#assign notiContent = "${symbol} has reached Floor Price : ${priceInfo}">
</#if>
<OneSignalTitle>Your Portfolio Alert</OneSignalTitle>${imagePath}  ${notiContent}