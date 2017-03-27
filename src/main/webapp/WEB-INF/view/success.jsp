<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<head>
    <base href="<%=basePath%>">
    <meta charset="UTF-8">
    <meta name="Generator" content="EditPlus®">
    <meta name="Keywords" content="">
    <meta name="Description" content="">
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>投诉</title>
    <link href="css/mybase.css" rel="stylesheet" type="text/css" />
    <style>
        .ts{margin-top:15px;margin-bottom: 15px;display: block;font-size: 14px;width: 90%;margin-left: auto;margin-right: auto;line-height:1.5;}
    </style>
</head>
<body class="c_center">
<br /><br />
    <img src="/images/btn/success_gou.png">
    <span class="ts font18">
        已提交
    </span>
    <span class="ts">
        您的投诉已提交审核，投诉单号：42999999.我们会在7个工作日内处理，感谢您对平台的支持。
    </span>
    <button class="layui-btn" style="width: 95%;">确定</button>
</body>
</html>
