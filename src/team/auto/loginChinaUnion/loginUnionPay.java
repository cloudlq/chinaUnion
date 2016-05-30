package team.auto.loginChinaUnion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class loginUnionPay {

    public static void main(String[] args) throws SQLException {
    	System.out.println("正在获取list...");
		List<Account> alist=getmlist("0");
		System.out.println(alist.size());
		List<Account> blist=new ArrayList<Account>();
		System.out.println("获取list成功");
		for(int i=0;i<alist.size();i++)
		{
			String account=alist.get(i).getAccount();
		    String password=alist.get(i).getPassword();
		    UnionPayClient uPayClient = new UnionPayClient(account, password);
	    	uPayClient.setImagePath("E://Eclipse_code//image");
	    	uPayClient.setTextPath("E://Eclipse_code//data");
	    	
	    	String ret = uPayClient.downloadAccountCheckWithDate("2015-12-01","2015-12-31");
	    	
	    	if(ret.equals("no such user") || ret.equals("ValidPassword"))
	    	{
	    		blist.add(alist.get(i));
	    	}
	    	System.out.println("第"+(i+1)+"个帐号返回值："+ ret);			
		}
		System.out.println("用户不存在或者用户密码错误的用户如下：");	
		for(int i = 0; i < blist.size();i++)
		{
			System.out.println("帐号:" + blist.get(i).getAccount() + "	密码：" + blist.get(i).getPassword());	
		}
    }
    
    //根据状态查找需要的list
    public static  List<Account> getmlist(String state) throws SQLException{
    	List<Account> mlist = new ArrayList<Account>();
    	try {
    		
    		//建立连接
    		Class.forName("com.mysql.jdbc.Driver");
    		String url="jdbc:mysql://114.55.72.18/unionpay";   
    		String user="yuancheng";        
    		String password="kpjf123456";   
    		Connection conn=DriverManager.getConnection(url, user, password);
    		Statement st=conn.createStatement(); 
    		
    		//查询数据抓取账户1
    		PreparedStatement  ps=conn.prepareStatement("select * from 银联账户  where 账号状态=?"); 
    		ps.setString(1, state);
    		
    		ResultSet rs=ps.executeQuery(); 
    		while(rs.next())  
    		{   
    			Account mi=new Account();
    			mi.setMerchantNumber(rs.getString(1));
    			mi.setMerchantName(rs.getString(2));
    			mi.setAccount(rs.getString(3));
    			mi.setPassword(rs.getString(4));
    			mi.setState(rs.getString(5));
    			mlist.add(mi);
    		} 
    		//6.释放资源   
    		rs.close();   
    		st.close();   
    		conn.close();  
    		
    		} catch (ClassNotFoundException e) {

    			e.printStackTrace();
    		}
		return mlist;
    	}   
  //批量更新账号状态
    public static void UpdateState(List<Account> mlist,String state) throws SQLException
    {
    	
      try {
			
			//建立连接
			Class.forName("com.mysql.jdbc.Driver");
			String url="jdbc:mysql://114.55.72.18/unionpay";   
			String user="yuancheng";        
			String password="kpjf123456";   
			Connection conn=DriverManager.getConnection(url, user, password);
			Statement st=conn.createStatement(); 
			//查询数据抓取账户1
			
			PreparedStatement  ps=conn.prepareStatement("update 银联账户 set 账号状态=? where 商户号=?"); 
			for(int i=0;i<mlist.size();i++){
			ps.setString(1,state); 
			ps.setString(2,mlist.get(i).getMerchantNumber()); 
			int j = ps.executeUpdate();
			if(j==0)
			{
				System.out.println("更新状态失败");
			}
			
			}
			//6.释放资源      
			st.close();   
			conn.close();  
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		
        }

}

