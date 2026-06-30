<@compress single_line=true>
KIS: status of
<#if sellBuyType == "BUY">
BUY
<#elseif sellBuyType == "SELL">
SELL
<#else>
</#if>
order ${orderId} symbol: ${symbol} placed by user ${username} changes to ${status}
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
with match qty ${matchQty} cancelled qty ${cancelledQty} and avgPrice ${avgPrice}
</@compress>