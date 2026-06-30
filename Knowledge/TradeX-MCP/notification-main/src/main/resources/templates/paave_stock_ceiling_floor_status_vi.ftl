<#if isCeilPrice>
    <#assign imagePath = "▲">
    <#assign notiContent = "${symbol} đã chạm giá trần : ${priceInfo}">
<#else>
    <#assign imagePath = "▼">
    <#assign notiContent = "${symbol} đã chạm giá sàn : ${priceInfo}">
</#if>
<OneSignalTitle>Chú Ý Danh Mục</OneSignalTitle>${imagePath}  ${notiContent}