<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
	<base href="<%=basePath%>">
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1" />
	<title>产品列表页面</title>
	<script type="text/javascript" src="js/common/jquery/jquery-1.9.1.js"></script>
	<style>
		#payewm{text-align: center;}
	</style>
</head>
<body>

<div style="text-align: center">
扫码支付
	<input type="hidden" value="${orderId}" id="orderId">
	<input type="hidden" value="${orderNo}" id="orderNo">
</div>
<div id="payewm">
	<img src="${imageUrl}">
</div>
<script>
	$(function(){
		setInterval("ajaxstatus()", 3000);
	});
	function ajaxstatus() {
		var url = "pay/orderfunc1.json";
		var orderId = $("#orderId").val();
		var orderNo = $("#orderNo").val();
		if(""==orderId){
			return;
		}
		$.get(url,{"orderId":orderId},function(data){
					if(data.code==200){
						if(data.obj==100){
							location.href="pay/toChb.html?orderId="+orderId+"&orderNo="+orderNo;
						}
					}
				}
		);
	}
</script>
</body>
</html>
