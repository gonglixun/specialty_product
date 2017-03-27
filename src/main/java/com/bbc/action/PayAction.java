package com.bbc.action;

import com.bbc.contants.ContantKey;
import com.bbc.entity.Orders;
import com.bbc.entity.Unifiedorder;
import com.bbc.entity.User;
import com.bbc.entity.bo.RespData;
import com.bbc.service.HongBaoService;
import com.bbc.service.OrdersService;
import com.bbc.service.UserService;
import com.bbc.tencent.common.Signature;
import com.bbc.tencent.common.XMLParser;
import com.bbc.util.*;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("pay")
public class PayAction {

	@Autowired
	private OrdersService ordersService;
	@Autowired
	private UserService userService;
	@Autowired
	private HongBaoService hongBaoService;

	/**
	 * 支付完成后的回调方法，该方法跳转到 拆红包页面。
	 * @return
	 */
	@RequestMapping(value = "/back")
	public String back(HttpServletRequest request) throws IOException {

		//读取参数
		InputStream inputStream ;
		StringBuffer sb = new StringBuffer();
		inputStream = request.getInputStream();
		String s ;
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		while ((s = in.readLine()) != null){
			sb.append(s);
		}
		in.close();
		inputStream.close();

		try {
			Map<String, Object> m = XMLParser.getMapFromXML(sb.toString());
			//过滤空 设置 TreeMap
			SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
			Iterator it = m.keySet().iterator();
			while (it.hasNext()) {
				String parameter = (String) it.next();
				Object parameterValue = m.get(parameter);

				String v = "";
				if(null != parameterValue) {
					v = parameterValue.toString().trim();
				}
				packageParams.put(parameter, v);
			}
			System.out.printf("packageParams = "+packageParams);
			//验证签名
			if (Signature.checkIsSignValidFromResponseString(sb.toString())){
				ordersService.updateOrder(m.get("out_trade_no").toString(),100);
				System.out.printf("数据合理");
			}else{
				System.out.printf("数据不合理");
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		//ordersService.getOrderByUserIdAndOrderId();
		return "view/chb";
	}


	/**
	 * 获得订单状态，判断支付是否成功
	 */
	@RequestMapping(value = "/orderfunc1")
	public @ResponseBody RespData orderfunc1(HttpServletRequest req,int orderId){
		Object sessionUser = req.getSession().getAttribute("user");
		User user = (User)sessionUser;
		Orders orders = ordersService.getOrderByUserIdAndOrderId(user.getId(),orderId);
		if(orders==null)
			return new RespData(500,"error");
		RespData respData = new RespData(200,"success");
		if(orders.getDataStatus()==100){
			respData.setObj(100);
			return respData;
		}else{
			respData.setObj(0);
			return respData;
		}
	}

	@RequestMapping(value = "/toChb")
	public String toChb(HttpServletRequest req,int orderId,String orderNo){
		req.setAttribute("orderId",orderId);
		req.setAttribute("orderNo",orderNo);
		return "view/chb";
	}


    @RequestMapping(value = "/getMoney")
    public @ResponseBody RespData getMoney(HttpServletRequest req,int orderId){
		//拆红包之前，验证订单状态。如果拆过一次，就提示不能拆红包了
		RespData respData = new RespData(100,"success");
		User user = (User)req.getSession().getAttribute("user");
		if(user==null){
			return new RespData(101,"登录过期");
		}
		Orders orders = ordersService.getOrderByUserIdAndOrderId(user.getId(),orderId);
		if(orders==null){
			return new RespData(102,"订单不存在或已拆红包");
		}
		if(orders.getDataStatus()==100){
			Map<String,Object> map = new HashMap<String,Object>();
			int hb_level = orders.getTotalFee().intValue();
			map.put("money",HBMoneyUtil.getMoney(hb_level));
			for (int i = 1;i<16;i++){
				map.put("m"+i,HBMoneyUtil.getMoneyBig(hb_level));
			}
			map.put("secret", DigestUtils.md5DigestAsHex((ContantKey.HB_SECRET+map.get("money")).getBytes()));
			respData.setObj(map);
			respData.setCode(200);
			return respData;
		}else if(orders.getDataStatus()==200){
			respData.setMsg("已经领过红包了");
			respData.setObj(200);
			return respData;
		}else if(orders.getDataStatus()==0){
			respData.setMsg("订单未支付");
			respData.setObj(0);
			return respData;
		}else{
			respData.setCode(500);
			respData.setMsg("未知异常，订单状态："+orders.getDataStatus());
			respData.setObj(-1);
			return respData;
		}
    }
	/**
	 * Created by gonglixun on 2016/12/13.
	 */
	@RequestMapping(value = "/test")
	public String toIndex(HttpServletRequest req,String attach,String totalFee){
		Object sessionUser = req.getSession().getAttribute("user");
		if(null!=sessionUser){
			User user = (User)sessionUser;
			String body = "开心拆一拆";
			String detail = "{'cost_price':"+totalFee+"}";
			if ("10".equals(totalFee)){

			}else if("20".equals(totalFee)){

			}else{
				return "view/error/error_500.jsp";
			}
			Orders orders = ordersService.addOrder(attach,new BigDecimal(totalFee),user.getId());

			String orderNo = orders.getOrderNo();

			totalFee = totalFee+"00";
			String nonceStr = "ibuaiVcKdpRxk";//随机数
			String ip = PCAddress.getIpAddress(req);
			String notifyUrl = ContantKey.notifyUrl;
			String tradeType = "NATIVE";
			String mchId = ContantKey.mchId;
			String charset = "utf-8";
			String appid = ContantKey.appID;
			String time_start = DateUtils.getFormatDate(new Date(),"yyyyMMddHHMMss");
			String time_expire = DateUtils.getFormatDate(DateUtils.getDate(1),"yyyyMMddHHMMss");


			SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
			parameters.put("appid",appid);
			parameters.put("mch_id",mchId);
			parameters.put("nonce_str",nonceStr);//随机数
			parameters.put("body", body);
			parameters.put("detail", detail);
			parameters.put("attach", attach);
			parameters.put("out_trade_no", orderNo);
			parameters.put("total_fee", totalFee);

			parameters.put("time_start", time_start);
			parameters.put("time_expire",  time_expire);
			parameters.put("notify_url", notifyUrl);//回调地址
			parameters.put("trade_type", tradeType);
			parameters.put("spbill_create_ip", ip);//IP地址

			String sign = WXSignUtils.createSign(charset,parameters);
			System.out.println("签名是："+sign);


			Unifiedorder unifiedorder = new Unifiedorder();
			unifiedorder.setAppid(appid);
			unifiedorder.setMch_id(mchId);
			unifiedorder.setNonce_str(nonceStr);
			unifiedorder.setSign(sign);
			unifiedorder.setBody(body);
			unifiedorder.setDetail(detail);
			unifiedorder.setAttach(attach);
			unifiedorder.setOut_trade_no(orderNo);
			unifiedorder.setTotal_fee(totalFee);
			unifiedorder.setIp(ip);
			unifiedorder.setTime_start(time_start);
			unifiedorder.setTime_expire(time_expire);
			unifiedorder.setNotify_url(notifyUrl);
			unifiedorder.setTrade_type(tradeType);
			//构造xml参数
			String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);


			String wxUrl = ContantKey.url_2;
			String method = "POST";

			String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();

			System.out.println(weixinPost);

			try {
				Map<String,Object> map = XMLParser.getMapFromXML(weixinPost);
				req.setAttribute("payPath",map.get("code_url"));
				try {
					String path = req.getSession().getServletContext().getRealPath("/");
					System.out.println("--------------------"+path);
					Map<String,String> m = QRUtil.createQR(map.get("code_url").toString(),ContantKey.width,ContantKey.height,path,orderNo);
					req.setAttribute("imageUrl",m.get("ml")+ File.separator+m.get("filename"));
				} catch (WriterException e) {
					e.printStackTrace();
				}
				req.setAttribute("orderId",orders.getId());
				req.setAttribute("orderNo",orderNo);
				return "view/pay";
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
//		String json = JsonUtil.xml2jsonString(weixinPost);
			return "view/error/error_500";
		}else{
			return "view/error/error_/500";
		}
	}

	@RequestMapping(value = "/toPay")
	public String toPay(HttpServletRequest req){
		return "view/pay";
	}

	/**
	 * 拆红包后，后台处理方法
	 */
    @RequestMapping(value = "/ph")
	public @ResponseBody  RespData ph(HttpServletRequest req,String s,BigDecimal totalFee,int orderId,String orderNo) throws Exception {
        Object obj = req.getSession().getAttribute("user");
		String ip = PCAddress.getIpAddress(req);
		System.out.println("===totalFee:"+totalFee);
		System.out.println("===密文:"+s);
		System.out.println("===明文加密后:"+DigestUtils.md5DigestAsHex((ContantKey.HB_SECRET+totalFee).getBytes()));
		if(null!=s&&s.equals(DigestUtils.md5DigestAsHex((ContantKey.HB_SECRET+totalFee).getBytes()))){
			if(null==obj){
				//会话失效
				return new RespData(401,"会话失效");
			}else{
				User user = (User)obj;
				//验证数据库里是否有订单
				Orders orders = ordersService.getOrderByUserIdAndOrderId(user.getId(),orderId);
				if(null==orders){
					return new RespData(402,"数据库里不存在这个订单");
				}else{
					if(orders.getDataStatus()==100) {
						ordersService.updateHandlerOrder(user.getId(), orderId, 200,user.getOpenid(),totalFee,orders.getOrderNo());
					/*  发送现金接口 */
//						SortedMap<Object,Object> parameters = WXSignUtils.createWeiParameters(orderNo,user.getOpenid(),totalFee.multiply(new BigDecimal(100)),ip);
//						String sign = WXSignUtils.createSign(ContantKey.chartsset,parameters);
//						parameters.put("sign",sign);
//						String xml = HttpXmlUtils.xmlInfoMap(parameters);
//						String xml = ;
						System.out.println("---- company pay start ---------------------------------------------------------------------------");
//						System.out.println("sign = "+ sign);
//						System.out.println("xml = "+ xml);
						String temp = "";
						try {
							temp = ClientCustomSSL.clientCustomSLL(ContantKey.mchId,ContantKey.url_3,getXML(orderNo,user.getOpenid(),totalFee,ip));
							hongBaoService.addHongBaoLog(user.getId(),orderId,totalFee,temp,0);
							System.out.println("---- company pay end  ---------------------------------------------------------------------------");
						} catch (Exception e) {
							e.printStackTrace();
						}



						//获得一级用户
						User user1J = userService.getUserByParentId(user.getParentId());
						User user2J = null;
						if(user1J != null){
							user2J = userService.getUserByParentId(user1J.getParentId());
						}
						BigDecimal user1YJ = new BigDecimal(1);
						BigDecimal user2YJ = new BigDecimal(1);
						/**
						 * 分发佣金
						 */
						BigDecimal orderTotalFee = orders.getTotalFee();
						if(orderTotalFee.compareTo(new BigDecimal(20))==0){
							user1YJ = new BigDecimal(2);
						}
						//一级返佣
						if(user1J!=null){
							temp = ClientCustomSSL.clientCustomSLL(ContantKey.mchId,ContantKey.url_3,getXML(orderNo+"Y"+user1J.getId(),user1J.getOpenid(),user1YJ,ip));
							hongBaoService.addHongBao(user1J.getOpenid(),user1J.getId(),user1YJ,1,orderNo+"Y"+user1J.getId());
							hongBaoService.addHongBaoLog(user1J.getId(),orderId,user1YJ,temp,1);
						}
						//二级返佣
						if(user2J!=null){
							temp = ClientCustomSSL.clientCustomSLL(ContantKey.mchId,ContantKey.url_3,getXML(orderNo+"Y"+user2J.getId(),user2J.getOpenid(),user2YJ,ip));
							hongBaoService.addHongBao(user2J.getOpenid(),user2J.getId(),user2YJ,2,orderNo+"Y"+user2J.getId());
							hongBaoService.addHongBaoLog(user2J.getId(),orderId,user2YJ,temp,2);
						}
						return new RespData(200, "");
					}else{
						return new RespData(403,"订单已经处理过，"+orders.getDataStatus());
					}
				}
			}
		}else{
			return new RespData(404,"金额有变化");
		}
    }

	public static String getXML(String orderNo,String oponid,BigDecimal totalFee,String ip){
		SortedMap<Object,Object> parameters = WXSignUtils.createWeiParameters(orderNo,oponid,totalFee.multiply(new BigDecimal(100)),ip);
		String sign = WXSignUtils.createSign(ContantKey.chartsset,parameters);
		parameters.put("sign",sign);
		String xml = HttpXmlUtils.xmlInfoMap(parameters);
		System.out.println(xml);
		return xml;
	}

	@RequestMapping(value = "/getUserOrderInfo")
	public @ResponseBody RespData getUserOrderInfo(HttpServletRequest req){
		//Object obj = req.getSession().getAttribute("user");
		//User user = (User)obj;
		Orders orders = ordersService.getOrderByUserId(14);
		if(orders!=null&&orders.getId()!=0){
			RespData data= new RespData(200,"");
			data.setObj(orders);
			return data;
		}else{
			return new RespData(100,"无待抽红包");
		}
	}
}
