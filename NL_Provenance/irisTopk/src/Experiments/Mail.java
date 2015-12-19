//package Experiments;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Properties;
//
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//
//public class Mail
//{
//	/*public static void main (String [] args) throws UnsupportedEncodingException
//	{
//		SendMail();
//	}*/
//
//	static String host, port, emailid,username, password;
//	static  Properties props = System.getProperties();
//	static Session l_session = null;
//
//	public static void SendMail(String content) {
//		host = "smtp.mail.yahoo.com";
//		port = "587";
//		emailid = "selp_cs@yahoo.com";
//		username = "selp_cs";
//		password = "Amiris11";
//		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//		emailSettings();
//		createSession();
//		sendMessage("selp_cs@yahoo.com", "amirgilad2233@gmail.com","Measurements " + dateFormat.format(Calendar.getInstance().getTime()), content);
//	}
//
//	public static void emailSettings() {
//		props.put("mail.smtp.host", "smtp.mail.yahoo.com");
//		props.put("mail.stmp.user", "selp_cs");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.password", "Amiris11");
//
//	}
//
//	public static void createSession() {
//
//		l_session = Session.getInstance(props,
//				new javax.mail.Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(username, password);
//			}
//		});
//
//		l_session.setDebug(false); // Disable the debug mode
//
//	}
//
//	public static boolean sendMessage(String emailFromUser, String toEmail, String subject, String msg)
//	{
//		try
//		{
//
//			MimeMessage message = new MimeMessage(l_session);
//			emailid = emailFromUser;
//
//			message.setFrom(new InternetAddress(emailid));
//
//			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
//			message.setSubject(subject);
//			msg = msg.replaceAll("(\r\n|\n)", "<br />");
//			message.setContent(msg, "text/html");
//
//			//message.setText(msg);
//			Transport.send(message);
//			System.out.println("Message Sent");
//		}
//		catch (MessagingException mex)
//		{
//			mex.printStackTrace();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}//end catch block
//		return true;
//	}
//}
