<@compress single_line=true>
KIS: Trạng thái lệnh
<#if sellBuyType == "BUY">
Mua
<#elseif sellBuyType == "SELL">
Bán
<#else>
</#if>
số ${orderId} mã: ${symbol}, đặt bởi ${username} thay đổi thành ${status}
<#if status == "REJ">
- Rejected
<#elseif status == "KLL">
- Killed
<#elseif status == "FLL">
<#if matchQty < qty>
- CANCELLED
<#else>
- FULLY FILLED
</#if>
<#elseif status == "CPD">
- Cancelled
<#elseif status == "CAN">
- Cancelled
<#else>

</#if>
với số lượng khớp ${matchQty}, số lượng đã hủy ${cancelledQty} và giá khớp trung bình ${avgPrice}
</@compress>