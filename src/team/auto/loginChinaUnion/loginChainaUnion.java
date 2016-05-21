package team.auto.loginUnionPay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;








import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

public class loginUnionPay {
	
    public static String getLoginUrl = "https://service.chinaums.com/uis/validateCode";
    
    public static String postLoginUrl = "https://service.chinaums.com/uis/uisWebLogin/login";
    
    public static String image_save_path = "F://WebCode//image//vcode.png";
    
    public static String userName = "898510175311007";
    
    public static String passWord = "Sc20150001";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DefaultHttpClient httpClient = new DefaultHttpClient();

		
		HttpGet httpGet = new HttpGet(getLoginUrl);
		
		try {
			//获取验证码
			HttpResponse validateCodeHttpResponse = httpClient.execute(httpGet);
			
			//将验证码保存
			download(validateCodeHttpResponse.getEntity().getContent(), image_save_path);
			//获取cookie
			//HttpEntity validateCodeEntity = validateCodeHttpResponse.getEntity();
//		    CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println(cookieStore);		
			
			
			//用户手动输入验证码
			System.out.println("请输入验证码");
			Scanner scanner = new Scanner(System.in);
			
			String identifyCode = scanner.nextLine();
			
//			scanner.close();
			
			//Post提交登录信息
			HttpPost httpPost = new HttpPost(postLoginUrl);
			
			//设置Post提交
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("userAcnt", userName));
            nvps.add(new BasicNameValuePair("userPwd", passWord));
            nvps.add(new BasicNameValuePair("validateCode", identifyCode));
            nvps.add(new BasicNameValuePair("submitFlag", "true"));
            
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            
            //执行post登录
            HttpResponse uisWebLoginResponse = httpClient.execute(httpPost);
            
//            //保存cookie
//            CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println("保存的cookie为" + cookieStore);
            
		    //打印登录输出信息
            HttpEntity uisWebLoginEntity = uisWebLoginResponse.getEntity();            
            StringBuilder result = new StringBuilder();  
            if (uisWebLoginEntity != null) {  
                InputStream instream = uisWebLoginEntity.getContent();  
                BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
                String temp = "";  
                while ((temp = br.readLine()) != null) {  
                    String str = new String(temp.getBytes(), "utf-8");  
                    result.append(str);  
                }  
            }  
            System.out.println("服务器返回登录信息为：" + result);
            
            //获取CST时间
            Date date = new Date();            
            String tempLastGetLoginUrl ="https://service.chinaums.com/uis/uisWebLogin/desktop?";
    		SimpleDateFormat df=new SimpleDateFormat("EEE'%20'MMM'%20'dd'%20'yyyy'%20'HH:mm:ss'%20''GMT+0800''%20'(zzz)", Locale.US);  
    		String dateStr = df.format(date); 
    		
    		//进入首页    		   		
    		String desktopUrl = tempLastGetLoginUrl + dateStr;            
            HttpGet desktopUrlHttpget = new HttpGet(desktopUrl); 
            HttpResponse desktopResponse = httpClient.execute(desktopUrlHttpget);            
            System.out.println("进入首页，服务器返回状态为" + desktopResponse.getStatusLine().getStatusCode());
            
            System.out.println("保存desktop.html");
         // Get hold of the response entity
            HttpEntity desktopEntity = desktopResponse.getEntity();
            // If the response does not enclose an entity, there is no need
            // to bother about connection release
            if (desktopEntity != null) {
                InputStream instream = desktopEntity.getContent();
                try {
                	//instream.read();
                	saveToFile("F://WebCode//html//", "desktop.html", instream);
                    // do something useful with the response
                } finally {
                    // Closing the input stream will trigger connection release
                    instream.close();
                }
            }
//          //保存cookie
//            CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println("保存的cookie为" + cookieStore); 
            
		    //https://service.chinaums.com/uis/uisWebLogin/toIndex
		    HttpGet toIndexHttpGet = new HttpGet("https://service.chinaums.com/uis/uisWebLogin/toIndex");
		    HttpResponse toIndexResponse = httpClient.execute(toIndexHttpGet);
		    System.out.println("进入toIndexHttpGet,服务器返回状态码为" + toIndexResponse.getStatusLine().getStatusCode());
		    
		    System.out.println("保存toIndex.html");
		 // Get hold of the response entity
            HttpEntity toIndexEntity = toIndexResponse.getEntity();
            // If the response does not enclose an entity, there is no need
            // to bother about connection release
            if (toIndexEntity != null) {
                InputStream instream = toIndexEntity.getContent();
                try {
                	//instream.read();
                	saveToFile("F://WebCode//html//", "toIndex.html", instream);
                    // do something useful with the response
                } finally {
                    // Closing the input stream will trigger connection release
                    instream.close();
                }
            }
		    
		    //https://service.chinaums.com/uis/accountCheckDetailQry/toAccountCheck
		    HttpGet toAccountCheckHttpGet = new HttpGet("https://service.chinaums.com/uis/accountCheckDetailQry/toAccountCheck");
		    HttpResponse toAccountCheckResponse = httpClient.execute(toAccountCheckHttpGet);
		    System.out.println("进入toAccountCheck,服务器返回状态码为" + toAccountCheckResponse.getStatusLine().getStatusCode());
		    
		    System.out.println("保存toAccountCheck.html");
		 // Get hold of the response entity
            HttpEntity toAccountCheckEntity = toAccountCheckResponse.getEntity();
            // If the response does not enclose an entity, there is no need
            // to bother about connection release
            if (toAccountCheckEntity != null) {
                InputStream instream = toAccountCheckEntity.getContent();
                try {
                    //instream.read();
                	saveToFile("F://WebCode//html//", "toAccountCheck.html", instream);
                    // do something useful with the response
                } finally {
                    // Closing the input stream will trigger connection release
                    instream.close();
                }
            }
		    
		    
		  //Post https://service.chinaums.com/uis/accountCheckDetailQry/qryAccountCheck
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
			qryAccountCheckNvps.add(new BasicNameValuePair("settDateBegin", "2016-05-19"));
			qryAccountCheckNvps.add(new BasicNameValuePair("settDateEnd", "2016-05-19"));
			qryAccountCheckNvps.add(new BasicNameValuePair("zdCode", null));
			qryAccountCheckNvps.add(new BasicNameValuePair("zdId", "-1"));
            	
            	
            qryAccountCheckHttpPost.setEntity(new UrlEncodedFormEntity(qryAccountCheckNvps, Consts.UTF_8));
            qryAccountCheckHttpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            
            //执行post  查账
            HttpResponse qryAccountCheckResponse = httpClient.execute(qryAccountCheckHttpPost);
            System.out.println("进入qryAccountCheck,服务器返回状态码为" + qryAccountCheckResponse.getStatusLine().getStatusCode());
            
            System.out.println("保存qryAccountCheck.html");
         // Get hold of the response entity
            HttpEntity qryAccountCheckEntity = qryAccountCheckResponse.getEntity();
            // If the response does not enclose an entity, there is no need
            // to bother about connection release
            if (qryAccountCheckEntity != null) {
                InputStream instream = qryAccountCheckEntity.getContent();
                try {
                    //instream.read();
                	saveToFile("F://WebCode//html//", "qryAccountCheck.html", instream);
                    // do something useful with the response
                } finally {
                    // Closing the input stream will trigger connection release
                    instream.close();
                }
            }
		    
          //用户手动输入验证码
			System.out.println("请输入CacheId");
			Scanner CacheIdScanner = new Scanner(System.in);
			
			String CacheIdStr = CacheIdScanner.nextLine();
			
			scanner.close();
			CacheIdScanner.close();
            
            //post https://service.chinaums.com/uis/viewReportServlet
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
	        HttpResponse viewReportResponse = httpClient.execute(viewReportHttpPost);
	        System.out.println("进入viewReport,服务器返回状态码为" + viewReportResponse.getStatusLine().getStatusCode());
	            
	        System.out.println("保存viewReport.html");
	         // Get hold of the response entity
	        HttpEntity viewReportEntity = viewReportResponse.getEntity();
	            // If the response does not enclose an entity, there is no need
	            // to bother about connection release
	        if (viewReportEntity != null) {
	            InputStream instream = viewReportEntity.getContent();
	            try {
	                    //instream.read();
	            	saveToFile("F://WebCode//html//", "viewReport.html", instream);
	                    // do something useful with the response
	            	} finally {
	                    // Closing the input stream will trigger connection release
	                    instream.close();
	                }
	            }
			
			httpGet.releaseConnection();
			
			httpPost.releaseConnection();
			
			desktopUrlHttpget.releaseConnection();
			
			//toIndexHttpGet.releaseConnection();
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static boolean download(InputStream in, String path)
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

	//将输入流中的内容输出到path指定的路径，fileName指定的文件名  
    private static void saveToFile(String path, String fileName, InputStream is) {  
        Scanner sc = new Scanner(is);  
        Writer os = null;  
        try {  
            os = new PrintWriter(path + fileName);  
            while (sc.hasNext()) {  
                os.write(sc.nextLine());  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (sc != null) {  
                sc.close();  
            }  
            if (os != null) {  
                try{  
                os.flush();  
                os.close();  
                }catch(IOException e){  
                    e.printStackTrace();  
                    System.out.println("输出流关闭失败！");  
                }  
            }  
        }  
    }  
}
