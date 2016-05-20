package team.auto.loginChinaUnion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

public class loginChainaUnion {
	
    public static String getLoginUrl = "https://service.chinaums.com/uis/validateCode";
    
    public static String postLoginUrl = "https://service.chinaums.com/uis/uisWebLogin/login";
    
    public static String image_save_path = "E://Eclipse_code//image//vcode.png";
    
    public static String userName = "898510175311007";
    
    public static String passWord = "Sc20150001";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		HttpGet httpGet = new HttpGet(getLoginUrl);
		
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			
			download(httpResponse.getEntity().getContent(), image_save_path);
			
			Scanner scanner = new Scanner(System.in);
			
			String identifyCode = scanner.nextLine();
			
			scanner.close();
			
			HttpPost httpPost = new HttpPost(postLoginUrl);
			
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("userAcnt", userName));
            nvps.add(new BasicNameValuePair("userPwd", passWord));
            nvps.add(new BasicNameValuePair("validateCode", identifyCode));
            nvps.add(new BasicNameValuePair("submitFlag", "true"));
            
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            HttpResponse response = httpClient.execute(httpPost);         
            
            String tempLastGetLoginUrl ="https://service.chinaums.com/uis/uisWebLogin/desktop?";
            
            HttpEntity entity = response.getEntity();  
            
            StringBuilder result = new StringBuilder();  
            if (entity != null) {  
                InputStream instream = entity.getContent();  
                BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
                String temp = "";  
                while ((temp = br.readLine()) != null) {  
                    String str = new String(temp.getBytes(), "utf-8");  
                    result.append(str);  
                }  
            }  
            System.out.println(result);
            
            Date date = new Date();
    		
    		SimpleDateFormat df=new SimpleDateFormat("EEE'%20'MMM'%20'dd'%20'yyyy'%20'HH:mm:ss'%20''GMT+0800''%20'(zzz)", Locale.US);  
    		
    		String dateStr = df.format(date);
    		
    		String lastGetLoginUrl = tempLastGetLoginUrl + dateStr;
            
            HttpGet lastHttpget = new HttpGet(lastGetLoginUrl);
 
            HttpResponse lastResponse = httpClient.execute(lastHttpget);
            
            System.out.println("Login form get: " + lastResponse.getStatusLine().getStatusCode());
 
            /*
            HttpEntity lastEntity = lastResponse.getEntity();  
              
            StringBuilder Rlastresult = new StringBuilder();  
            if (lastEntity != null) {  
                InputStream instream = lastEntity.getContent();  
                BufferedReader br = new BufferedReader(new InputStreamReader(instream));  
                String temp = "";  
                while ((temp = br.readLine()) != null) {  
                    String str = new String(temp.getBytes(), "utf-8");  
                    Rlastresult.append(str);  
                }  
            }  
            System.out.println(Rlastresult); 
			*/
			httpGet.releaseConnection();
			
			httpPost.releaseConnection();
			
			lastHttpget.releaseConnection();
            
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

}
