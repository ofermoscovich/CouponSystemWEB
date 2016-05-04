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
//import javax.servlet.http.HttpSession;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import activities.CouponSystem;
import beans.ClientType;
import beans.Company;
import beans.Coupon;
import beans.CouponType;
//import beans.DBDAO.CouponDBDAO;
//import clients.CouponClientFacade;
import clients.CompanyFacade;
import entityDB.Income;
//import entityDB.IncomePK;
//import entityDB.IncomeServiceBean;
import entityDB.IncomeType;
import main.CouponException;
import activities.AppUtil;

@Path("/company")
public class CompanyService {

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
	/**
	 * Constructor
	 */
	public CompanyService(){
		businessDelegate = new BusinessDelegate();
		appUtil = new AppUtil();
	}

//	public void startSession() {
//		HttpSession session = req.getSession(true);
//	}

	/**
	 * Company Login check
	 * @param name String 
	 * @param Password String 
	 * @return String - Facade - the specific customer Facade (this).
	 **/	
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("companyLogin")
	public String login(@QueryParam("user") String user,
						@QueryParam("pass") String pass) {
		//CompanyFacade companyFacade = null;
		try {
			CompanyFacade companyFacade = (CompanyFacade) CouponSystem.getInstance().login(user, pass, ClientType.COMPANYFACADE);
			if (companyFacade != null){
				HttpSession session = req.getSession(false);
				if(session != null){
					session.invalidate();
				}
				session = req.getSession(true);
				session.setAttribute("facade", companyFacade);
				return "login as " + user + " Success!\nWelcome " + user + ".";
			}
		} catch (CouponException e) {
			return "Company login Falied! " + e.getMessage();
		}
		return "Company login Falied!";
	}

	/**
	 * Create coupon web service.
	 * Will also create income entry.
	 * @param title
	 * @param startDate
	 * @param endDate
	 * @param type
	 * @param amount
	 * @param message
	 * @param price
	 * @param image
	 * @return String
	 */
	//@POST
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("createCoupon")
	public String createCoupon(
			@QueryParam("title") String title,
			@QueryParam("startDate") Timestamp startDate,
			@QueryParam("endDate") Timestamp endDate,
			@QueryParam("type") CouponType type,
			@QueryParam("amount") long amount,
			@QueryParam("message") String message,
			@QueryParam("price") double price,
			@QueryParam("image") String image) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Cteate Coupon Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
					
				Coupon coupon = new Coupon();
				coupon.setTitle(title);
				coupon.setStartDate(startDate);
				coupon.setEndDate(endDate);
				coupon.setType(type);
				coupon.setAmount(amount);
				coupon.setMessage(message);
				coupon.setPrice(price);
				coupon.setImage(image);
	
				companyFacade.createCoupon(coupon);
	
				/**
				 * Create Income 100
				 */
				
				String compName = companyFacade.getCompanyInstance().getCompName();
	
				Income income = new Income();
				income.setId(companyFacade.getCompanyId());
				income.setName(compName); 
				income.setDate(appUtil.today());
				income.setDescription(IncomeType.COMPANY_NEW_COUPON);
				income.setAmount(100);
			
				businessDelegate.storeIncome(income);
				return coupon.toString() + "\n" + income.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Cteate Coupon Failed: No Action Made.\nPlease Check The Problem.";
		}
		//return "Cteate Coupon: No Action Made.\nPlease Check The Problem.";
	}	

	/**
	 * Remove coupon web service.
	 * @param id - long coupon id
	 * @return String
	 */
	//@POST
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces({MediaType.APPLICATION_JSON})
	@Path("removeCoupon")
	public String removeCoupon(@QueryParam("coupId") long coupId) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Remove Coupon Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				companyFacade.removeCoupon(coupId);
				return "Coupon " + coupId + " Removed.";
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Remove Coupon Failed: No Action Made.\nPlease Check The Problem. ";
		}
	}	 	

	/**
	 * Update coupon web service.
	 * Will also create income entry.
	 * @param endDate
	 * @param price
	 * @return String
	 */
	//@PUT
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) // need to change to object mediatype.application_json
	@Produces({MediaType.APPLICATION_JSON})
	@Path("updateCoupon")
	public String updateCoupon(@QueryParam("coupId") long coupId,
							   @QueryParam("endDate") Timestamp endDate,
							   @QueryParam("price") double price) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Update Coupon Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
	
				Coupon coupon = new Coupon();
				coupon.setId(coupId);
				coupon.setEndDate(endDate);
				coupon.setPrice(price);
	
				companyFacade.updateCoupon(coupon);
	
				/**
				 * Create Income 10
				 */
				
				String compName = companyFacade.getCompanyInstance().getCompName();
	
				Income income = new Income();
				income.setId(companyFacade.getCompanyId());
				income.setName(compName); 
				income.setDate(appUtil.today());
				income.setDescription(IncomeType.COMPANY_UPDATE_COUPON);
				income.setAmount(10);
			
				businessDelegate.storeIncome(income);
				return coupon.toString() + "\n" + income.toString();
			}
		} catch (CouponException e) {
			e.printStackTrace();
			return "Update Coupon Failed: No Action Made.\nPlease Check The Problem. ";
		}
	}	

	/**
	 * View specific company by id
	 * @return Company
	 * @throws CouponException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCompany")
	public String getCompany() {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Getting Company Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Company company = companyFacade.getCompany();
						
				return company.toString();
			}
			
		} catch (CouponException e) {
			e.printStackTrace();
			return "Getting Company Failed: \nPlease Check The Problem. " + e.getMessage();
		}
	}
		
	/**
	 * View specific coupon
	 * @param coupId long 
	 * @return Coupon
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCoupon")
	public String getCoupon(@QueryParam("coupId") long coupId) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Coupon Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Coupon coupon = companyFacade.getCoupon(coupId);
						
				return coupon.toString();
			}
			
		} catch (CouponException e) {
			//e.printStackTrace();
			return "Retrieve Company Coupon Failed! \nPlease Check The Problem." + e.getMessage();
		}
	}	

	/**
	 * Get all company coupons.
	 * @return Coupons collection.
	 * @throws CouponException
	 * false - not expired coupons.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCoupons")
	public String getCoupons() {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Coupons Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Set<Coupon> coupons = companyFacade.getCoupons();
						
				return coupons.toString();
			}
			
		} catch (CouponException e) {
			//e.printStackTrace();
			return "Retrieve Company Coupons Failed! \nPlease Check The Problem." + e.getMessage();
		}
	}

	/**
	 * View all coupons related to specific company by type.
	 * @param coupType CouponType 
	 * @return Coupons collection.
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCouponsByType")
	public String getCouponsByType(@QueryParam("type") CouponType coupType) {
		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Coupons By Type Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Set<Coupon> coupons = companyFacade.getCouponsByType(coupType);
						
				return coupons.toString();
			}
			
		} catch (CouponException e) {
			//e.printStackTrace();
			return "Retrieve Company Coupons By Type Failed! \nPlease Check The Problem. " + e.getMessage();
		}
	}
		
	/**
	 * View all coupons related to specific company By Max Coupon Price
	 * @param price double 
	 * @return Coupons collection.
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCouponsByMaxCouponPrice")
	public String getCouponsByMaxCouponPrice(@QueryParam("price") double price) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Coupons By Type Price Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Set<Coupon> coupons = companyFacade.getCouponsByMaxCouponPrice(price);
						
				return coupons.toString();
			}
			
		} catch (CouponException e) {
			//e.printStackTrace();
			return "Retrieve Company Coupons By Type Price Failed! \nPlease Check The Problem. " + e.getMessage();
		}
	}

	/**
	 * View all coupons related to specific company by Max Coupon Date
	 * @param maxCouponDate Timestamp 
	 * @return Coupons collection.
	 * @throws CouponException
	 */
	@GET
	@Consumes({MediaType.TEXT_PLAIN})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getCouponsByMaxCouponDate")
	public String getCouponsByMaxCouponDate(@QueryParam("date") Timestamp maxCouponDate) {

		try {
			HttpSession session = req.getSession(false);
			if(session == null){
				try {
					res.sendRedirect(loginPage);
				} catch (IOException e) {
					e.printStackTrace();
					return "Retrieve Company Coupons By Max Date Failed: Invalis Session";
				}
				return "Please re-login!";
				
			} else {

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				
				Set<Coupon> coupons = companyFacade.getCouponsByMaxCouponDate(maxCouponDate);
						
				return coupons.toString();
			}
			
		} catch (CouponException e) {
			//e.printStackTrace();
			return "Retrieve Company Coupons By Max Date Failed! \nPlease Check The Problem. " + e.getMessage();
		}
	}		

	/**
	 * View Income By Company
	 * @return Income - Collection
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("viewAllIncomeByCompany")
	public String viewAllIncomeByCompany() {
		
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

				CompanyFacade companyFacade = (CompanyFacade) session.getAttribute("facade");
				List<Income> incomes = null;
	 
				incomes = businessDelegate.viewAllIncomeByCompany(companyFacade.getCompanyId(),
						   										  companyFacade.getCompanyInstance().getCompName());
				return incomes.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "View Income By Customer Failed!" + e.getMessage();
		}
	}
}
