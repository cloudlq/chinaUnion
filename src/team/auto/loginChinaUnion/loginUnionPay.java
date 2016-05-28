package team.auto.loginUnionPay;

public class loginUnionPay {

    public static void main(String[] args) {
    	UnionPayClient uPayClient = new UnionPayClient("898510175311007", "Sc20150001");
    	uPayClient.setImagePath("F://WebCode//image//vcode.jpg");
    	uPayClient.setTextPath("F://WebCode//Data");
    	
    	Boolean ret = uPayClient.downloadAccountCheck(10);
    	
    	System.out.println(ret);
    }
}