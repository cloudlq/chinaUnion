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
			//��ȡ��֤��
			HttpResponse validateCodeHttpResponse = httpClient.execute(httpGet);
			
			//����֤�뱣��
			download(validateCodeHttpResponse.getEntity().getContent(), image_save_path);
			//��ȡcookie
			//HttpEntity validateCodeEntity = validateCodeHttpResponse.getEntity();
//		    CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println(cookieStore);		
			
			
			//�û��ֶ�������֤��
			System.out.println("��������֤��");
			Scanner scanner = new Scanner(System.in);
			
			String identifyCode = scanner.nextLine();
			
//			scanner.close();
			
			//Post�ύ��¼��Ϣ
			HttpPost httpPost = new HttpPost(postLoginUrl);
			
			//����Post�ύ
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("userAcnt", userName));
            nvps.add(new BasicNameValuePair("userPwd", passWord));
            nvps.add(new BasicNameValuePair("validateCode", identifyCode));
            nvps.add(new BasicNameValuePair("submitFlag", "true"));
            
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            
            //ִ��post��¼
            HttpResponse uisWebLoginResponse = httpClient.execute(httpPost);
            
//            //����cookie
//            CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println("�����cookieΪ" + cookieStore);
            
		    //��ӡ��¼�����Ϣ
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
            System.out.println("���������ص�¼��ϢΪ��" + result);
            
            //��ȡCSTʱ��
            Date date = new Date();            
            String tempLastGetLoginUrl ="https://service.chinaums.com/uis/uisWebLogin/desktop?";
    		SimpleDateFormat df=new SimpleDateFormat("EEE'%20'MMM'%20'dd'%20'yyyy'%20'HH:mm:ss'%20''GMT+0800''%20'(zzz)", Locale.US);  
    		String dateStr = df.format(date); 
    		
    		//������ҳ    		   		
    		String desktopUrl = tempLastGetLoginUrl + dateStr;            
            HttpGet desktopUrlHttpget = new HttpGet(desktopUrl); 
            HttpResponse desktopResponse = httpClient.execute(desktopUrlHttpget);            
            System.out.println("������ҳ������������״̬Ϊ" + desktopResponse.getStatusLine().getStatusCode());
            
            System.out.println("����desktop.html");
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
//          //����cookie
//            CookieStore cookieStore = httpClient.getCookieStore();
//		    httpClient.setCookieStore(cookieStore);
//		    System.out.println("�����cookieΪ" + cookieStore); 
            
		    //https://service.chinaums.com/uis/uisWebLogin/toIndex
		    HttpGet toIndexHttpGet = new HttpGet("https://service.chinaums.com/uis/uisWebLogin/toIndex");
		    HttpResponse toIndexResponse = httpClient.execute(toIndexHttpGet);
		    System.out.println("����toIndexHttpGet,����������״̬��Ϊ" + toIndexResponse.getStatusLine().getStatusCode());
		    
		    System.out.println("����toIndex.html");
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
		    System.out.println("����toAccountCheck,����������״̬��Ϊ" + toAccountCheckResponse.getStatusLine().getStatusCode());
		    
		    System.out.println("����toAccountCheck.html");
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
							
			//����Post�ύbody
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
            
            //ִ��post  ����
            HttpResponse qryAccountCheckResponse = httpClient.execute(qryAccountCheckHttpPost);
            System.out.println("����qryAccountCheck,����������״̬��Ϊ" + qryAccountCheckResponse.getStatusLine().getStatusCode());
            
            System.out.println("����qryAccountCheck.html");
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
		    
          //�û��ֶ�������֤��
			System.out.println("������CacheId");
			Scanner CacheIdScanner = new Scanner(System.in);
			
			String CacheIdStr = CacheIdScanner.nextLine();
			
			scanner.close();
			CacheIdScanner.close();
            
            //post https://service.chinaums.com/uis/viewReportServlet
            HttpPost viewReportHttpPost = new HttpPost("https://service.chinaums.com/uis/viewReportServlet");
          //����Post�ύbody
			List<NameValuePair> viewReportNvps = new ArrayList<NameValuePair>();
			viewReportNvps.add(new BasicNameValuePair("action", "3"));
			viewReportNvps.add(new BasicNameValuePair("cacheId", CacheIdStr));
			viewReportNvps.add(new BasicNameValuePair("height", "0"));
			viewReportNvps.add(new BasicNameValuePair("width", "988px"));
			viewReportNvps.add(new BasicNameValuePair("reportName", "c2V0dGxlUmVjb3JkU2VhcmNoVGVtcGxhdGUvMjAxMzA5MDkwMDAwMDAwMS5yYXE,"));
			viewReportNvps.add(new BasicNameValuePair("busiTypeIdList", "1"));
           
			viewReportHttpPost.setEntity(new UrlEncodedFormEntity(viewReportNvps, Consts.UTF_8));
			viewReportHttpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
	            
	            //ִ��post  ����
	        HttpResponse viewReportResponse = httpClient.execute(viewReportHttpPost);
	        System.out.println("����viewReport,����������״̬��Ϊ" + viewReportResponse.getStatusLine().getStatusCode());
	            
	        System.out.println("����viewReport.html");
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

	//���������е����������pathָ����·����fileNameָ�����ļ���  
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
                    System.out.println("������ر�ʧ�ܣ�");  
                }  
            }  
        }  
    }  
}
