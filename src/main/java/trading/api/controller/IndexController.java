package trading.api.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import trading.api.model.OutputResult;
import trading.api.model.UserInput;
import trading.api.stocks.BankNiftyFutures;
import trading.api.stocks.BankNiftyOptionSelling;
import trading.api.stocks.NiftyFutures;
import trading.api.stocks.NiftyOptionSelling;
import trading.api.utility.connection.BreezeConnect;

@Controller
public class IndexController {

	@Autowired
	OutputResult outputResult;
	@Autowired
	private NiftyFutures niftyFutures;
	@Autowired
	private BankNiftyFutures bankNiftyFutures;
	@Autowired
	private NiftyOptionSelling niftyOptionSelling;
	@Autowired
	private BankNiftyOptionSelling bankNiftyOptionSelling;

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/login")
	public String login(Model model, @ModelAttribute UserInput userInput) throws Exception {

		// String userId = "8667648336";
		String secretKey = "VB691uN4h06Gs122843(148955681DR4";
		String apiKey = "4H51704z6Y4_25!427t796e57418$q3F";

		BreezeConnect breezeConnect = new BreezeConnect(apiKey);

		// https://api.icicidirect.com/apiuser/login?api_key=4H51704z6Y4_25!427t796e57418$q3F

		System.out.println("https://api.icicidirect.com/apiuser/login?api_key="
				+ URLEncoder.encode(apiKey, StandardCharsets.UTF_8));

		// Generate Session
		breezeConnect.generateSession(secretKey, userInput.getSessionKey());

		// Format Year-Month-Date
		String todaysDate = userInput.getTodaysDate();
		String fromDate = userInput.getFromDate();
		String toDate = userInput.getToDate();

		String niftyFutExpiryDate = userInput.getNiftyFutExpiryDate();
		String bNFutExpiryDate = userInput.getBNFutExpiryDate();

		String[] niftySellingExpiryDate = new String[4];
		niftySellingExpiryDate[0] = userInput.getNiftySellingExpiryDate0();
		niftySellingExpiryDate[1] = userInput.getNiftySellingExpiryDate1();
		niftySellingExpiryDate[2] = userInput.getNiftySellingExpiryDate2();
		niftySellingExpiryDate[3] = userInput.getNiftySellingExpiryDate3();

		String[] bNSellingExpiryDate = new String[4];
		bNSellingExpiryDate[0] = userInput.getBNSellingExpiryDate0();
		bNSellingExpiryDate[1] = userInput.getBNSellingExpiryDate1();
		bNSellingExpiryDate[2] = userInput.getBNSellingExpiryDate2();
		bNSellingExpiryDate[3] = userInput.getBNSellingExpiryDate3();

		if (userInput != null && !userInput.getSessionKey().isBlank() && !userInput.getFromDate().isBlank()
				&& !userInput.getToDate().isBlank()) {
			if (!userInput.getNiftyFutExpiryDate().isBlank()) {
				niftyFutures.getNiftyFutData(breezeConnect, fromDate, toDate, niftyFutExpiryDate);
			}
			if (!userInput.getBNFutExpiryDate().isBlank()) {
				bankNiftyFutures.getBankNiftyFutData(breezeConnect, fromDate, toDate, bNFutExpiryDate);
			}
			if (!userInput.getNiftySellingExpiryDate0().isBlank() && !userInput.getNiftySellingExpiryDate1().isBlank()
					&& !userInput.getNiftySellingExpiryDate2().isBlank()
					&& !userInput.getNiftySellingExpiryDate3().isBlank()) {
				niftyOptionSelling.getNiftyOptionSellingData(breezeConnect, fromDate, toDate, niftySellingExpiryDate);
			}
			if (!userInput.getBNSellingExpiryDate0().isBlank() && !userInput.getBNSellingExpiryDate1().isBlank()
					&& !userInput.getBNSellingExpiryDate2().isBlank()
					&& !userInput.getBNSellingExpiryDate3().isBlank()) {
				bankNiftyOptionSelling.getBankNiftyOptionSellingData(breezeConnect, fromDate, toDate,
						bNSellingExpiryDate);
			}
		}

		model.addAttribute("a", outputResult.getFuturesNiftyBuyEntry());
		model.addAttribute("b", outputResult.getFuturesNiftySellEntry());
		model.addAttribute("c", outputResult.getFuturesBankNiftyBuyEntry());
		model.addAttribute("d", outputResult.getFuturesBankNiftySellEntry());

		model.addAttribute("e", outputResult.getSellingNiftyCeEntry());
		model.addAttribute("f", outputResult.getSellingNiftyPeEntry());
		model.addAttribute("g", outputResult.getSellingBankNiftyCeEntry());
		model.addAttribute("h", outputResult.getSellingBankNiftyPeEntry());

		return "result";
	}

}
