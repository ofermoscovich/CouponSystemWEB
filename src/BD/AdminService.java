package BD;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

//import com.sun.mail.handlers.text_plain;

import activities.CouponSystem;
import beans.ClientType;
import beans.*;
import clients.AdminFacade;
import entityDB.Income;
import main.CouponException;
import activities.AppUtil;

@Path("/admin")
public class AdminService {

	BusinessDelegate businessDelegate;
	//Income income;
	Timestamp date;
	AppUtil appUtil;
	@Context
	private HttpServletRequest req;
	@Context
	private HttpServletResponse res;
	@Context
	private ServletContext ctx;
	
	String loginPage = "../../index.html";
//	String loginPage = "http://localhost:8080/CouponSystemWEB/rest/admin/adminLogin?user=admin&pass=1234";
	/**
	 * Constructor
	 */
	public AdminService(){
		businessDelegate = new BusinessDelegate();
		appUtil = new AppUtil();
	}

//	public void startSession() {
//		HttpSession session = req.getSession(true);
//	}
	

	/**
	 * Admin Login check
	 * @param name String 
	 * @param Password String 
	 * @return String - Facade - the specific customer Facade (this).
	 **/	
	@GET
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("adminLogin")
	public String login(@QueryParam("user") String user,
						@QueryParam("pass") String pass) {

		try {
			AdminFacade adminFacade = (AdminFacade) CouponSystem.getInstance().login(user, pass, ClientType.ADMINFACADE);
			if (adminFacade != null){
				HttpSession session = req.getSession(false);
				if(session != null){
					session.invalidate();
				}
				session = req.getSession(true);
				session.setAttribute("facade", adminFacade);
				System.out.println("WE DID IT!!!!!!!!!!!!");
				return "login as " + user + " Success!\nWelcome " + user + ".";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Admin login Falied! " + e.getMessage();
		}
		return "Admin login Falied!";
	}

	/**
	 * Add Company with unique name.
	 * @param name - String - Company 
	 * @param password - String - Company 
	 * @param email - String - Company 
	 * @throws CouponException
	 * Company must include unique name, filled password.
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("createCompany")
	public String createCompany(@QueryParam("name") String name,
								@QueryParam("password") String password,
								@QueryParam("email") String email) {
		
		try {
			
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Create Company Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Company company = new Company();
				company.setCompName(name);
				company.setPassword(password);
				company.setEmail(email);
				adminFacade.createCompany(company);
				return "Company " + name + " Succesfully Created. ";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Create Company Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 	
	
	/**
	 * Delete Company with all related coupons (CompanyCoupon and CustomerCoupon tables).
	 * @param compId long - companyId
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("removeCompany")
	public String removeCompany(@QueryParam("compId") long compId) {

		try {
			
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Remove Company Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				String name = adminFacade.getCompany(compId).getCompName();
				adminFacade.removeCompany(compId);
				return "Company " + name + " Succesfully Removed. ";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Remove Company Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 	
	
	/**
	 * Update Company (without company name)
	 * @param company Company 
	 * @throws CouponException
	 * Company must include same unique name and filled password.
	 * Include double check to eliminate updating the company name, since 
	 *  the Company object does not pass the name anyway and the sql Update
	 *  query does not include the company name.
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("updateCompany")
	public String updateCompany(@QueryParam("compId") long compId,
								@QueryParam("password") String password,
			  				    @QueryParam("email") String email) {
		
		try {
			
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Update Company Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Company company = new Company();
				company.setId(compId);
				company.setPassword(password);
				company.setEmail(email);
				adminFacade.updateCompany(company);
				return "Company " + adminFacade.getCompany(compId).getCompName() + " Succesfully Updated. ";
			
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Update Company Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 	
	
	/**
	 * View specific company by id // show specific company detail 
	 * @param compId long - company Id
	 * @return Company company
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getCompany")
	public String getCompany(@QueryParam("compId") long compId) {

		try {
			
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				Company company = adminFacade.getCompany(compId);
				return company.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrieve Company Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 	
	/**
	 * View all companies
	 * @return Company collection 
	 * @throws CouponException
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getAllCompanies")
	public String getAllCompanies() {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve All Companies Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				Set<Company> companies = adminFacade.getAllCompanies();
				return companies.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrieve All Companies Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 	
	
	
	/**
	 * Add new customer with unique name and filled password.
	 * @param customer Customer 
	 * @throws CouponException
	 * Customer Name and password are required.
	 * Customer name must be unique.
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("createCustomer")
	public String createCustomer(@QueryParam("name") String name,
							     @QueryParam("password") String password) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Create Customer Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Customer customer = new Customer();
				customer.setCustName(name);
				customer.setPassword(password);
	
				adminFacade.createCustomer(customer);
				return "Customer " + name + " Succesfully Created. ";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Create Customer Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 		
	
	/**
	 * Delete customer with all his current and history coupons
	 * @param custId long 
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("removeCustomer")
	public String removeCustomer(@QueryParam("custId") long custId) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Remove Customer Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				String name = adminFacade.getCustomer(custId).getCustName();
				adminFacade.removeCustomer(custId);
				return "Customer " + name + " Succesfully Removed. ";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Remove Customer Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}
	
	/**
	 * Update customer without the name.
	 * @param customer Customer 
	 * @throws CouponException
	 * Customer Name and password are required.
	 * Customer name must be unique.
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("updateCustomer")
	public String updateCustomer(@QueryParam("custId") long custId,
							     @QueryParam("password") String password) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Update Customer Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Customer customer = new Customer();
				customer.setId(custId);
				customer.setPassword(password);
				String name = adminFacade.getCustomer(custId).getCustName();
				adminFacade.updateCustomer(customer);
				return "Customer " + name + " Succesfully Updated. ";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Update Customer Failed: No Action Made.\nPlease Check The Problem. " + e.getMessage();
		}
	}	 		
	
		
	/**
	 * View list of all customers
	 * @return customer collection
	 * @throws CouponException
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getAllCustomers")
	public String getAllCustomers() {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrive All Customers Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Set<Customer> customers = adminFacade.getAllCustomers();
				return customers.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrive All Customers Failed: \nPlease Check The Problem. " + e.getMessage();
		}
	}	 		

	/**
	 * View specific customer detail
	 * @param custId long 
	 * @return Customer 
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getCustomer")
	public String getCustomer(@QueryParam("custId") long custId) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrive Customers Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Customer customer = adminFacade.getCustomer(custId);
				return customer.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrive Customer Failed: \nPlease Check The Problem. " + e.getMessage();
		}
}	 		
	/**
	 * View one coupon
	 * @param coupId long 
	 * @return Coupon
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getCoupon")
	public String getCoupon(@QueryParam("coupId") long coupId) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrive Coupon Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Coupon coupon = adminFacade.getCoupon(coupId);
				return coupon.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrive Coupon Failed: \nPlease Check The Problem. " + e.getMessage();
		}
	}	 		
	
	/**
	 * View all valid coupons in coupon table
	 * @return Coupon collection
	 * @throws CouponException
	 * false = not expired
	 * This Coupon list important to administrator for support.
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getCoupons")
	public String getCoupons() {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrive Coupons Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Set<Coupon> coupons = adminFacade.getCoupons();
				return coupons.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrive Coupons Failed: \nPlease Check The Problem. " + e.getMessage();
		}
	}	 		
	
	/**
	 * View All Coupons in the system.
	 * @return Coupon collection
	 * @throws CouponException
	 * True = also expired
	 * This Coupon list important to administrator for support. 
	 * For Administrator or support. (Not In Use Yet)
	 * If company Id, customerId and couponId are all 0 than all companies will be pooled.
	 * true - Include expired coupons - all coupons in Coupon table (for administrator only) 
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getAllCoupons")
	public String getAllCoupons() {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrive All Coupons Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
		
				Set<Coupon> coupons = adminFacade.getAllCoupons();
				return coupons.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Retrive All Coupons Failed: \nPlease Check The Problem. " + e.getMessage();
		}
	}	 		
		
	/**
	 * View Income By Company 
	 * @return String - Income collection
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("viewAllIncomeByCompany")
	public String viewIncomeByCompany(@QueryParam("compId") long compId) {
		
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "View Income By Customer Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				List<Income> incomes = null;
	 
				incomes = businessDelegate.viewAllIncomeByCompany(compId,
																  adminFacade.getCompany(compId).getCompName());
				return incomes.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "View Income By Customer Failed!" + e.getMessage();
		}
	}	 	  

	/**
	 * View Income By customer 
	 * @return String - Income collection
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("viewAllIncomeByCustomer")
	public String viewIncomeByCustomer(@QueryParam("custId") long custId) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "View Income By Customer Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {
				
				AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				List<Income> incomes = null;
				incomes = businessDelegate.viewAllIncomeByCustomer(custId,
																 adminFacade.getCustomer(custId).getCustName());
				return incomes.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "View Income By Customer Failed!" + e.getMessage();
		}
	}	 	

	/**
	 * View All Income 
	 * @return String - Income collection
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("viewAllIncome")
	public String viewAllIncome() {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "View All Income Failed Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {
				
				//AdminFacade adminFacade = (AdminFacade) session.getAttribute("facade");
				List<Income> incomes = null;
				incomes = businessDelegate.viewAllIncome();
				return incomes.toString();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "View All Income Failed!" + e.getMessage();
		}
	}			
}
