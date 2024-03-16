package trading.api.utility.connection;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import trading.api.constants.CommonConstants;
import trading.api.entity.NseEntity;
import trading.api.stocks.BankNiftyFutures;
import trading.api.stocks.BankNiftyOptionSelling;
import trading.api.stocks.NiftyFutures;
import trading.api.stocks.NiftyOptionSelling;

@RestController
@RequestMapping(value = "/api/v1/")
public class LoginController {

	@Autowired
	private NiftyFutures niftyFutures;
	@Autowired
	private BankNiftyFutures bankNiftyFutures;
	@Autowired
	private NiftyOptionSelling niftyOptionSelling;
	@Autowired
	private BankNiftyOptionSelling bankNiftyOptionSelling;

	@GetMapping(value = "/login")
	public void UserLogin() throws Exception {
		
		// String userId = "8667648336";
		String secretKey = "VB691uN4h06Gs122843(148955681DR4";
		String apiKey = "4H51704z6Y4_25!427t796e57418$q3F";

		BreezeConnect breezeConnect = new BreezeConnect(apiKey);

		// https://api.icicidirect.com/apiuser/login?api_key=4H51704z6Y4_25!427t796e57418$q3F

		System.out.println("https://api.icicidirect.com/apiuser/login?api_key="
				+ URLEncoder.encode(apiKey, StandardCharsets.UTF_8));

		// Generate Session 
		breezeConnect.generateSession(secretKey, "37119461");

		//Format Year-Month-Date
		String fromDate = "2024-03-13";
		String toDate = "2024-03-14";
		
		//GapUp GapDown
		//String todaysDate = "2024-02-06";
		
		String niftyFutExpiryDate = "2024-03-28";
		String bNFutExpiryDate = "2024-03-28";
		
		String[] niftySellingExpiryDate= new String[4];
		niftySellingExpiryDate[0] = "2024-03-21";
		niftySellingExpiryDate[1] = "2024-03-28";
		niftySellingExpiryDate[2] = "2024-04-04";
		niftySellingExpiryDate[3] = "2024-04-10";
		
		String[] bNSellingExpiryDate= new String[4];
		bNSellingExpiryDate[0] = "2024-03-20";
		bNSellingExpiryDate[1] = "2024-03-27";
		bNSellingExpiryDate[2] = "2024-04-03";
		bNSellingExpiryDate[3] = "2024-04-10";
			
	//	niftyFutures.getNiftyFutData(breezeConnect, fromDate, toDate, niftyFutExpiryDate);
	//	niftyOptionSelling.getNiftyOptionSellingData(breezeConnect, fromDate, toDate, niftySellingExpiryDate);
		
	//	bankNiftyFutures.getBankNiftyFutData(breezeConnect, fromDate, toDate, bNFutExpiryDate);
	//	bankNiftyOptionSelling.getBankNiftyOptionSellingData(breezeConnect, fromDate, toDate, bNSellingExpiryDate);
		
		String todaysDate = "2024-03-15";
		
		niftyFutures.getNiftyFutDataGapUp(breezeConnect, fromDate, toDate, todaysDate, niftyFutExpiryDate);

	}
}
