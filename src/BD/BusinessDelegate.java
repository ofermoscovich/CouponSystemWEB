package BD;

import java.util.Hashtable;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import entityDB.Income;
import entityDB.IncomeServiceLocal;

public class BusinessDelegate {
	
	private IncomeServiceLocal stub;
	private QueueSession qsession;
	private QueueSender qsender;
	
	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private Queue queue;
	
	/**
	 * initialize all jms class members and
	 *
	 *	connect to the Queue - using the configured JNDI name
	 *	start connection
	 *	enter text here
	 * @throws NamingException 
	 * @throws JMSException 
	 */
	public BusinessDelegate ()  {
		try {
			InitialContext ctx = getInitialContext(); //new InitialContext()
			stub=(IncomeServiceLocal)ctx.lookup("incomeSrv/local");
			qconFactory = (QueueConnectionFactory)ctx.lookup("ConnectionFactory");
			qcon = qconFactory.createQueueConnection();
			qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // true - transacted???
			queue = (Queue)ctx.lookup("queue/couponSystem-Queue");
			qsender = qsession.createSender(queue);
		}catch(NamingException e1){e1.printStackTrace();
		}catch(JMSException e2){e2.printStackTrace();}
	}
	
	public void storeIncome(Income income) {	
		try {
			ObjectMessage om = qsession.createObjectMessage(income);
			qsender.send(om);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized List<Income> viewAllIncome() {
		return stub.viewAllIncome();
	}
	
	public synchronized List<Income> viewAllIncomeByCompany(long companyId, String name) {
		return stub.viewAllIncomeByCompany(companyId,name);
	}
	
	public synchronized List<Income> viewAllIncomeByCustomer(long customerId, String name) {
		return stub.viewAllIncomeByCustomer(customerId,name);
	}
	
	public synchronized InitialContext getInitialContext(){
		Hashtable<String,String> h=new Hashtable<String,String>();
		h.put("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
		h.put("java.naming.provider.url","localhost");
		try {
			return new InitialContext(h);
		} catch (NamingException e) {
			System.out.println("Error! Cannot generate InitialContext");
			e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		try{
			qsender.close();
			qsession.close();
			qcon.close();
		}catch(JMSException e1){e1.printStackTrace();
		}catch(NullPointerException e2){e2.printStackTrace();}
	}
}
