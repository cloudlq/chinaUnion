package team.auto.loginChinaUnion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class UnionPayClient {
	
	private DefaultHttpClient httpClient;
    
	private String userName;
    
	private String passWord;
	
	private String imagePath;
	
	private String textPath;

	public UnionPayClient(String user_name, String pass_word){
		this.userName = user_name;
		this.passWord = pass_word;
		this.httpClient = new DefaultHttpClient();
	}
	
	/*
	 * 
	 * 1、获取验证码
	 * 2、登录
	 * 3、进入首页
	 * 4、提交设置的起始日期，查对账明细, 获取CacheId
	 * 5、提交运算报表请求
	 * 6、下载对账单
	 * 
	 * @return
	 * success:对账单下载成功
	 * input error：参数输入有误
	 * no such user：没有该用户
	 * ValidPassword：密码错误
	 * CacheId error：CacheId错误
	 * qryAccountCheck error：对账单请求失败 
	 * login error：未知登录错误
	 */
	public String downloadAccountCheck(int days){
		String retStr;
		if(days <= 0){			
			System.err.println("查询账单日期需为正整数");
			retStr = "input error";
			return retStr;
		}
			
		if(imagePath == null || textPath == null){
			System.err.println("请先设置imagePath和textPath");
			retStr = "input error";
			return retStr;
		}
		
		while(true) {
			//验证码
			String validateCode = getValidateCode(imagePath);
			//验证码错误，重新识别验证码
			if(validateCode.equals("identify error")){
				continue;
			}
			//登陆
			String loginRetString = loginUnionPay(validateCode);
			/*
			 * @return 
			 * LoginSuccess:登陆成功
			 * LoginError:登陆失败
			 * ValidPassword:密码错误
			 * ValidCode:验证码错误
			 * NoSuchUser:没有该用户
			 */
			if(loginRetString.equals("LoginSuccess")){
				//登陆成功
				
				//进入首页
				intoDesktop();
				
				//获取起始日期
				Date date = new Date();
				Calendar calendarBegin = Calendar.getInstance(); //得到日历
				calendarBegin.setTime(date);//把当前时间赋给日历
				calendarBegin.add(Calendar.DAY_OF_MONTH, - days);  //设置为前一天
				Date dBegin = calendarBegin.getTime();   //得到前一天的时间
				SimpleDateFormat BeginDate=new SimpleDateFormat("yyyy-MM-dd");  
				String settDateBegin = BeginDate.format(dBegin);
				
				//获取前一天日期,settDateEnd
				Calendar calendarEnd = Calendar.getInstance(); //得到日历
				calendarEnd.setTime(date);//把当前时间赋给日历
				calendarEnd.add(Calendar.DAY_OF_MONTH, - 1);  //设置为前一天
				Date dEnd = calendarEnd.getTime();   //得到前一天的时间
				SimpleDateFormat endDate=new SimpleDateFormat("yyyy-MM-dd");  
				String settDateEnd = endDate.format(dEnd);
				
				//根据日期查找对账明细, 获取CacheId
		    	String cacheIdString = accountCheckWithDate(settDateBegin, settDateEnd);
		    	
		    	//CacheId获取失败
		    	if(cacheIdString.equals("error")){
		    		System.err.println("CacheId获取失败");
		    		retStr = "CacheId error";
					return retStr;
		    	}
		    	
		    	//提交运算报表请求
		    	String qryretString = requestAccountCheck(cacheIdString);
		    	//运算报表请求失败
		    	if(qryretString.equals("error")){
		    		System.err.println("运算报表请求失败");
		    		retStr = "qryAccountCheck error";
					return retStr;
		    	}
		    	
		    	downloadText(cacheIdString, textPath);
		    	
		    	retStr = "success";
				return retStr;
			}else if(loginRetString.equals("ValidCode")){
				//验证码错误
				System.err.println(loginRetString);
				continue;
			}else if(loginRetString.equals("NoSuchUser")){
				//没有该用户
				System.err.println("登陆失败："+loginRetString);
				retStr = "no such user";
				return retStr;
			}else if(loginRetString.equals("ValidPassword")){
				//密码错误
				System.err.println("登陆失败："+loginRetString);
				retStr = "ValidPassword";
				return retStr;
			}else{
				//登陆失败
				System.err.println("登陆失败："+loginRetString);
				retStr = "login error";
				return retStr;
			}
		}
	}

	/*
	 * 
	 * 1、获取验证码
	 * 2、登录
	 * 3、进入首页
	 * 4、提交设置的起始日期，查对账明细, 获取CacheId
	 * 5、提交运算报表请求
	 * 6、下载对账单
	 * 
	 * @return
	 * success:对账单下载成功
	 * input error：参数输入有误
	 * no such user：没有该用户
	 * ValidPassword：密码错误
	 * CacheId error：CacheId错误
	 * qryAccountCheck error：对账单请求失败 
	 * login error：未知登录错误
	 */
	public String downloadAccountCheckWithDate(String settDateBegin, String settDateEnd){
		String retStr;
			
		if(imagePath == null || textPath == null){
			System.err.println("请先设置imagePath和textPath");
			retStr = "input error";
			return retStr;
		}
		//判断日期是否符合规范
        String rexp = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
         
        Pattern BeginPat = Pattern.compile(rexp);  
         
        Matcher BeginMat = BeginPat.matcher(settDateBegin);  
         
        boolean BeginDate = BeginMat.matches();
        
        Pattern EndPat = Pattern.compile(rexp);  
        
        Matcher EndMat = EndPat.matcher(settDateBegin);  
         
        boolean EndDate = EndMat.matches();
        
        if(!BeginDate || !EndDate){
        	System.err.println("设置正确的日期格式：yyyy-MM-dd");
        	retStr = "input error";
			return retStr;
        }
		
        while(true) {
			//验证码
			String validateCode = getValidateCode(imagePath);
			//验证码错误，重新识别验证码
			if(validateCode.equals("identify error")){
				continue;
			}
			//登陆
			String loginRetString = loginUnionPay(validateCode);
			/*
			 * @return 
			 * LoginSuccess:登陆成功
			 * LoginError:登陆失败
			 * ValidPassword:密码错误
			 * ValidCode:验证码错误
			 * NoSuchUser:没有该用户
			 * ServerError：系统处理失败
			 */
			if(loginRetString.equals("LoginSuccess")){
				//登陆成功
				
				//进入首页
				intoDesktop();
				
				//根据日期查找对账明细, 获取CacheId
		    	String cacheIdString = accountCheckWithDate(settDateBegin, settDateEnd);
		    	
		    	//CacheId获取失败
		    	if(cacheIdString.equals("error")){
		    		System.err.println("CacheId获取失败");
		    		retStr = "CacheId error";
					return retStr;
		    	}
		    	
		    	//提交运算报表请求
		    	String qryretString = requestAccountCheck(cacheIdString);
		    	//运算报表请求失败
		    	if(qryretString.equals("error")){
		    		System.err.println("运算报表请求失败");
		    		retStr = "qryAccountCheck error";
					return retStr;
		    	}
		    	
		    	downloadExcel(cacheIdString, textPath);
		    	
		    	retStr = "success";
				return retStr;
			}else if(loginRetString.equals("ValidCode")){
				//验证码错误
				System.err.println(loginRetString);
				continue;
			}else if(loginRetString.equals("ServerError")){
				//系统处理失败
				System.err.println("系统处理失败："+loginRetString);
				continue;
			}else if(loginRetString.equals("NoSuchUser")){
				//没有该用户
				System.err.println("登陆失败："+loginRetString);
				retStr = "no such user";
				return retStr;
			}else if(loginRetString.equals("ValidPassword")){
				//密码错误
				System.err.println("登陆失败："+loginRetString);
				retStr = "ValidPassword";
				return retStr;
			}else{
				//登陆失败
				System.err.println("登陆失败："+loginRetString);
				retStr = "login error";
				return retStr;
			}
		}
	}

	private String getValidateCode(String pathName){
		
		String validateCodeUrl = "https://service.chinaums.com/uis/validateCode";
		
		HttpGet httpGet = new HttpGet(validateCodeUrl);	
		
		//获取验证码
		HttpResponse validateCodeHttpResponse;
		try {
			validateCodeHttpResponse = httpClient.execute(httpGet);
			download(validateCodeHttpResponse.getEntity().getContent(), pathName);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
				
		//System.out.println("识别验证码...");
		
		//识别验证码
		String identifyCode;
		String imageResult = createByPost("Hi_TuDou", "ds19910926", "1040", "90", "54544", "69727e89294f4facb6bb3507737523bb", pathName);
		if(imageResult.length() <= 0) {
    		//System.out.println("识别验证码错误：未知问题");
			identifyCode = "identify error" ;
			return identifyCode;
    	}
		
    	Document dm;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			dm = db.parse(new ByteArrayInputStream(imageResult.getBytes("utf-8")));
			NodeList resultNl = dm.getElementsByTagName("Result");
			NodeList errorNl = dm.getElementsByTagName("Error");
			
			if(resultNl.getLength() > 0 ) {
				identifyCode = String.format("%s", resultNl.item(0).getFirstChild().getNodeValue());
				
			} else if (errorNl.getLength() > 0) {
				//System.out.println("识别验证码错误：未知问题");
				identifyCode = "identify error";
			} else {
				//System.out.println("识别验证码错误：未知问题");
				identifyCode = "identify error";
			}
	        
		} catch (Exception e) {
			//System.out.println("识别验证码错误：XML解析错误");
			identifyCode = "identify error";
			e.printStackTrace();
		}
		httpGet.releaseConnection();
		return identifyCode;
	}
	
	/*
	 * @return 
	 * LoginSuccess:登陆成功
	 * LoginError:登陆失败
	 * ValidPassword:密码错误
	 * ValidCode:验证码错误
	 * NoSuchUser:没有该用户
	 * ServerError:系统处理失败
	 */
	private String loginUnionPay(String validateCode){
		
		String LoginUrl = "https://service.chinaums.com/uis/uisWebLogin/login";
		
		String resultString = null;
		//Post提交登录信息
		HttpPost httpPost = new HttpPost(LoginUrl);
		
		//设置Post提交
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("userAcnt", userName));
        nvps.add(new BasicNameValuePair("userPwd", passWord));
        nvps.add(new BasicNameValuePair("validateCode", validateCode));
        nvps.add(new BasicNameValuePair("submitFlag", "true"));
        
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        
        //执行post登录
		try {
			HttpResponse uisWebLoginResponse = httpClient.execute(httpPost);
			HttpEntity uisWebLoginEntity = uisWebLoginResponse.getEntity();
	        // If the response does not enclose an entity, there is no need
	        // to bother about connection release
	        if (uisWebLoginEntity != null) {
	            String resDataString = EntityUtils.toString(uisWebLoginEntity);
	            JSONObject resJson = new JSONObject(resDataString);
	            String respCode = resJson.getString("respCode");	            
	            /*
	             * {  "respCode" : "000000"}
	             * {  "respCode" : "000001",  "respDesc" : "该用户不存在"}
	             * {  "respCode" : "1000002",  "respDesc" : "用户登录验证失败1次，错误次数达到5次将被锁定！"}
	             * {  "respCode" : "000001",  "respDesc" : "验证码不正确，请重新输入"}
	             * {  "respCode" : "9999",  "respDesc" : "系统处理失败"}
	             */
	            
	            if(respCode.equals("000000")){
	            	resultString = "LoginSuccess";
	            }else if(respCode.equals("1000002")){
	            	resultString = "ValidPassword";
				}else if(respCode.equals("000001")){
					String respDesc =  resJson.getString("respDesc");
					if(respDesc.equals("验证码不正确，请重新输入")){
						resultString = "ValidCode";
					} else if(respDesc.equals("该用户不存在")){
						resultString = "NoSuchUser";
					}else{
						resultString = "LoginError";
					}
				}else if(respCode.equals("9999")){
					resultString = "ServerError";
				}else{
					resultString = "LoginError";
				}
	            return resultString;        
	        }//end if
		}// end try
		 catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpPost.releaseConnection();
		resultString = "LoginError";
		return resultString;		
	}
	
	private void intoDesktop(){
		
        String tempLastGetLoginUrl ="https://service.chinaums.com/uis/uisWebLogin/desktop?";
        
        Date date = new Date();
 		SimpleDateFormat df=new SimpleDateFormat("EEE'%20'MMM'%20'dd'%20'yyyy'%20'HH:mm:ss'%20''GMT+0800''%20'(zzz)", Locale.US);  
 		String dateStr = df.format(date);
 		
 		//获取首页链接URL
 		String desktopUrl = tempLastGetLoginUrl + dateStr;
 		
        HttpGet desktopUrlHttpget = new HttpGet(desktopUrl); 
        HttpResponse desktopResponse;
		try {
			desktopResponse = httpClient.execute(desktopUrlHttpget);
			HttpEntity desktopEntity = desktopResponse.getEntity();
			InputStream instream = desktopEntity.getContent();
			instream.read();
			//do something
			instream.close();
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		desktopUrlHttpget.releaseConnection();
	}
	
	public String accountCheckWithDate(String settDateBegin, String settDateEnd){
		String cacheIdString = "";
		HttpPost qryAccountCheckHttpPost = new HttpPost("https://service.chinaums.com/uis/accountCheckDetailQry/qryAccountCheck");
		
		//设置Post提交body
		List<NameValuePair> qryAccountCheckNvps = new ArrayList<NameValuePair>();
		qryAccountCheckNvps.add(new BasicNameValuePair("amount1", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("amount2", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("bankCardNo1", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("bankCardNo2", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("batchNo", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("busiTypeIdList", "1"));
		qryAccountCheckNvps.add(new BasicNameValuePair("dealDateBegin", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("dealDateEnd", null));	
		qryAccountCheckNvps.add(new BasicNameValuePair("dealType", null));	
		qryAccountCheckNvps.add(new BasicNameValuePair("fdId", "-1"));
		qryAccountCheckNvps.add(new BasicNameValuePair("fkhNo", null));	
		qryAccountCheckNvps.add(new BasicNameValuePair("groupCode", "undefined"));	
		qryAccountCheckNvps.add(new BasicNameValuePair("hasForm", "true"));	
		qryAccountCheckNvps.add(new BasicNameValuePair("merCode", "undefined"));
		qryAccountCheckNvps.add(new BasicNameValuePair("searchNo", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("searchObj", "2"));
		qryAccountCheckNvps.add(new BasicNameValuePair("settDateBegin", settDateBegin));
		qryAccountCheckNvps.add(new BasicNameValuePair("settDateEnd", settDateEnd));
		qryAccountCheckNvps.add(new BasicNameValuePair("zdCode", null));
		qryAccountCheckNvps.add(new BasicNameValuePair("zdId", "-1"));
        	
        	
        qryAccountCheckHttpPost.setEntity(new UrlEncodedFormEntity(qryAccountCheckNvps, Consts.UTF_8));
        qryAccountCheckHttpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        
        //执行post  查账 获取CacheId
        try {
			HttpResponse qryAccountCheckResponse = httpClient.execute(qryAccountCheckHttpPost);
			
			HttpEntity qryAccountCheckEntity = qryAccountCheckResponse.getEntity();          
            if (qryAccountCheckEntity != null) {
                InputStream instream = qryAccountCheckEntity.getContent();
                Scanner sc = new Scanner(instream);
                //匹配得到cacheId
                String urlPattern = "\"cacheId\":\"A_[0-9]*\"";
                Pattern pattern = Pattern.compile(urlPattern);
                while (sc.hasNext()) {
                    String tempString = sc.nextLine();
                    Matcher matcher = pattern.matcher(tempString);
                    while(matcher.find()){
                    	cacheIdString = matcher.group();
                    }
                }               	
                instream.close();
                sc.close();
            }
            if(cacheIdString.length() < 15)
            {
            	//System.out.println("识别cacheId失败：" + cacheIdString);
            	cacheIdString = "error";
            	return cacheIdString;
            }
            cacheIdString = cacheIdString.substring(11, cacheIdString.length() - 1);
            return cacheIdString;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        qryAccountCheckHttpPost.releaseConnection();
        cacheIdString = "error";
		return cacheIdString;
	}
	
	private String requestAccountCheck(String CacheIdStr){
		String resultString = null;
		HttpPost viewReportHttpPost = new HttpPost("https://service.chinaums.com/uis/viewReportServlet");
        //设置Post提交body
		List<NameValuePair> viewReportNvps = new ArrayList<NameValuePair>();
		viewReportNvps.add(new BasicNameValuePair("action", "3"));
		viewReportNvps.add(new BasicNameValuePair("cacheId", CacheIdStr));
		viewReportNvps.add(new BasicNameValuePair("height", "0"));
		viewReportNvps.add(new BasicNameValuePair("width", "988px"));
		viewReportNvps.add(new BasicNameValuePair("reportName", "c2V0dGxlUmVjb3JkU2VhcmNoVGVtcGxhdGUvMjAxMzA5MDkwMDAwMDAwMS5yYXE,"));
		viewReportNvps.add(new BasicNameValuePair("busiTypeIdList", "1"));
         
		viewReportHttpPost.setEntity(new UrlEncodedFormEntity(viewReportNvps, Consts.UTF_8));
		viewReportHttpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
	            
	    //执行post  查账
		try {
			HttpResponse viewReportResponse = httpClient.execute(viewReportHttpPost);
			HttpEntity viewReportEntity = viewReportResponse.getEntity();
			String resDataString = EntityUtils.toString(viewReportEntity);
			JSONObject resJson = new JSONObject(resDataString);
            Boolean status = resJson.getBoolean("status");
            
            /*
             * {"pageCount":1,"status":true}
             */
            
            if(status){
            	resultString = "success";
			}else{
				resultString = "error";
			}
            
            return resultString;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		viewReportHttpPost.releaseConnection();
		return null;		
	}

	private void downloadText(String CacheIdStr, String pathName){
		Date date = new Date();
        SimpleDateFormat txtDatefDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");        
		String textString = txtDatefDateFormat.format(date);  
        String saveTxtName = null;
		try {
			saveTxtName = Base64.encodeBase64String(textString.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        HttpGet getTxtHttpGet = new HttpGet("https://service.chinaums.com/uis/viewReportServlet?action=6&format=text&isNeedFormula=0&dispRatio=100&reportName=c2V0dGxlUmVjb3JkU2VhcmNoVGVtcGxhdGUvMjAxMzA5MDkwMDAwMDAwMS5yYXE%2C&cacheId="+CacheIdStr+"&saveAsName="+saveTxtName+"&textDataSeparator=%7C&textDataLineBreak=%0D%0A&excelPageStyle=0&excelFormat=2003&wordFormat=2003&excelUsePaperSize=yes&width=988px&height=0&columns=13&pdfExportStyle=text%2C1&backAndRefresh=yes");
        
        HttpResponse getTxtResponse;
		try {
			getTxtResponse = httpClient.execute(getTxtHttpGet);
			HttpEntity getTxtEntity = getTxtResponse.getEntity();
	        if (getTxtEntity != null) {
	            InputStream instream = getTxtEntity.getContent();
	            download(instream, pathName+ "//" +textString+".text");
	            instream.close();	            
	        }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getTxtHttpGet.releaseConnection();
	}

	private void downloadExcel(String CacheIdStr, String pathName){
		Date date = new Date();
        SimpleDateFormat excelDatefDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");        
		String excelString = excelDatefDateFormat.format(date);  
        String saveExcelName = null;
		try {
			saveExcelName = Base64.encodeBase64String(excelString.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        HttpGet getExcelHttpGet = new HttpGet("https://service.chinaums.com/uis/viewReportServlet?action=6&format=excel&isNeedFormula=0&dispRatio=100&reportName=c2V0dGxlUmVjb3JkU2VhcmNoVGVtcGxhdGUvMjAxMzA5MDkwMDAwMDAwMS5yYXE%2C&cacheId="+CacheIdStr+"&saveAsName="+saveExcelName+"&textDataSeparator=%7C&textDataLineBreak=%0D%0A&excelPageStyle=0&excelFormat=2003&wordFormat=2003&excelUsePaperSize=yes&width=988px&height=0&columns=13&pdfExportStyle=text%2C1&backAndRefresh=yes");
        
        HttpResponse getExcelResponse;
		try {
			getExcelResponse = httpClient.execute(getExcelHttpGet);
			HttpEntity getExcelEntity = getExcelResponse.getEntity();
	        if (getExcelEntity != null) {
	            InputStream instream = getExcelEntity.getContent();
	            download(instream, pathName+ "//" +excelString+".xls");
	            instream.close();	            
	        }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		getExcelHttpGet.releaseConnection();
	}
	
	public  String getUserName() {
		return userName;
	}

	public  void setUserName(String userName) {
		this.userName = userName;
	}

	public  String getPassWord() {
		return passWord;
	}

	public  void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath + "//vcode.jpg";
	}

	public String getTextPath() {
		return textPath;
	}

	public void setTextPath(String textPath) {
		this.textPath = textPath;
	}
	
	private boolean download(InputStream in, String path)
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(path);
            byte b[] = new byte[1024];
            int j = 0;
            while ((j = in.read(b)) != -1)
            {
                out.write(b, 0, j);
            }
            out.flush();
            File file = new File(path);
            if(file.exists() && file.length() == 0)
                return false;
            return true;
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            if("FileNotFoundException".equals(e.getClass().getSimpleName()))
                System.err.println("download FileNotFoundException");
            if("SocketTimeoutException".equals(e.getClass().getSimpleName()))
                System.err.println("download SocketTimeoutException");
            else
                e.printStackTrace();
        } finally{
             
            if(out != null)
                try
                {
                    out.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            if(in != null)
                try
                {
                    in.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
        return false;
    }
	
	/*  以下为识别验证码*/
	/**
	 * 字符串MD5加密
	 * @param s 原始字符�?
	 * @return  加密后字符串
	 */
	private String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 答题
	 * @param url 			请求URL，不带参�? 如：http://api.ruokuai.com/create.xml
	 * @param param			请求参数，如：username=test&password=1
	 * @param data			图片二进制流
	 * @return				平台返回结果XML样式 
	 * @throws IOException
	 */
	private  String httpPostImage(String url, String param,
			byte[] data) throws IOException {
		long time = (new Date()).getTime();
		URL u = null;
		HttpURLConnection con = null;
		String boundary = "----------" + MD5(String.valueOf(time));
		String boundarybytesString = "\r\n--" + boundary + "\r\n";
		OutputStream out = null;
		
		u = new URL(url);
		
		con = (HttpURLConnection) u.openConnection();
		con.setRequestMethod("POST");
		//con.setReadTimeout(95000);   
		con.setConnectTimeout(95000); //此�?�与timeout参数相关，如果timeout参数�?90秒，这里就是95000，建议多5�?
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(true);
		con.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		
		out = con.getOutputStream();
			
		for (String paramValue : param.split("[&]")) {
			out.write(boundarybytesString.getBytes("UTF-8"));
			String paramString = "Content-Disposition: form-data; name=\""
					+ paramValue.split("[=]")[0] + "\"\r\n\r\n" + paramValue.split("[=]")[1];
			out.write(paramString.getBytes("UTF-8"));
		}
		out.write(boundarybytesString.getBytes("UTF-8"));

		String paramString = "Content-Disposition: form-data; name=\"image\"; filename=\""
				+ "sample.gif" + "\"\r\nContent-Type: image/gif\r\n\r\n";
		out.write(paramString.getBytes("UTF-8"));
		
		out.write(data);
		
		String tailer = "\r\n--" + boundary + "--\r\n";
		out.write(tailer.getBytes("UTF-8"));

		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(con
					.getInputStream(), "UTF-8"));
		String temp;
		while ((temp = br.readLine()) != null) {
			buffer.append(temp);
			buffer.append("\n");
		}

		return buffer.toString();
	}
	
	/**
	 * 上传题目图片返回结果	
	 * @param username		用户�?
	 * @param password		密码
	 * @param typeid		题目类型
	 * @param timeout		任务超时时间
	 * @param softid		软件ID
	 * @param softkey		软件KEY
	 * @param filePath		题目截图或原始图二进制数据路�?
	 * @return
	 * @throws IOException
	 */
	private  String createByPost(String username, String password,
			String typeid, String timeout, String softid, String softkey,
			String filePath) {
		String result = "";
		String param = String.format(
				"username=%s&password=%s&typeid=%s&timeout=%s&softid=%s&softkey=%s",
				username, password, typeid, timeout, softid, softkey);
		try {
			File f = new File(filePath);
			if (null != f) {
				int size = (int) f.length();
				byte[] data = new byte[size];
				FileInputStream fis = new FileInputStream(f);
				fis.read(data, 0, size);
				if(null != fis) fis.close();
				
				if (data.length > 0)	
					result = httpPostImage("http://api.ruokuai.com/create.xml", param, data);
			}
		} catch(Exception e) {
			result = "未知问题";
		}		
		return result;
	}
	
}
