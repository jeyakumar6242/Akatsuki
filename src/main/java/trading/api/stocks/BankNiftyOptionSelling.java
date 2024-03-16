package trading.api.stocks;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import trading.api.constants.CommonConstants;
import trading.api.entity.NseEntity;
import trading.api.entity.NseValues;
import trading.api.utility.connection.BreezeConnect;

@Component
public class BankNiftyOptionSelling {

	public void getBankNiftyOptionSellingData(BreezeConnect breezeConnect, String fromDate, String toDate,
			String[] bNSellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.BANKNIFTY, CommonConstants.NSE, "", "", "", "");

		BankNiftyOptionSellingCalculations(breezeConnect, nseEntity, strFromDate, strToDate, bNSellingExpiryDate);
		System.out.println("-----------------------------");
	}

	private void BankNiftyOptionSellingCalculations(BreezeConnect breezeConnect, NseEntity nseEntity,
			String strFromDate, String strToDate, String[] bNSellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		if (nseEntity.getObito().size() == 2) {
			List<NseValues> obito = nseEntity.getObito();

			double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
					: obito.get(1).getHigh();
			double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
					: obito.get(1).getLow();

			int callBuffer = (int) (two2DLL * CommonConstants.BankNiftyCallStrikeFactor);
			int putBuffer = (int) (two2DHH * CommonConstants.BankNiftyPutStrikeFactor);

			int callEndStrike = (callBuffer / 100) * 100; // Round Down
			int putEndStrike = ((putBuffer / 100) + 1) * 100; // Round Up

			getBankNiftySellingCallData(breezeConnect, callEndStrike, strFromDate, strToDate, bNSellingExpiryDate);
			getBankNiftySellingPutData(breezeConnect, putEndStrike, strFromDate, strToDate, bNSellingExpiryDate);

		} else {
			System.out.println("Incorrect From and TO date. Please check.");
		}
	}

	private String getBankNiftySellingCallData(BreezeConnect breezeConnect, int callEndStrike, String strFromDate,
			String strToDate, String[] bNSellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startCallStrike = callEndStrike + 900;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startCallStrike * CommonConstants.BankNiftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
						CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
						String.valueOf(startCallStrike));

				// System.out.println(nseEntity);

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					// System.out.println("Expiry : " + bNSellingExpiryDate[i] + " | Strike price: "
					// + startCallStrike
					// + " | Minimum Premium: " + Math.round(minimumPremium) + " | 2DLL: " +
					// Math.round(two2DLL)
					// + " | " + obito.get(0).getLow() + " | " + obito.get(1).getLow());
					// System.out.println(obito.get(1).openInterest);

					if (two2DLL > minimumPremium) {
						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						System.out.println("\n\nBANK NIFTY CALL OPTION SELLING");
						System.out.println("BANK NIFTY CE :" + startCallStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + Math.round(entry));
						System.out.println("Target   : " + Math.round(target));
						System.out.println("Stoploss : " + Math.round(stoploss));
						System.out.println("MSL      : " + Math.round(msl));
						System.out.println("TSL      : " + Math.round(tsl));
						// System.out.println(obito.get(0).getLow() + " " + obito.get(1).getLow() + " "
						// + (callEndStrike + 900) + " " + minimumPremium);

						return "";
					}

				} else {
					System.out.println("NO Data found for BANK NIFTY " + startCallStrike + " CE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startCallStrike = startCallStrike - 100;
			}
		}
		System.out.println("Not Found : NO NIFTY CE Call Today");
		return "";
	}

	private String getBankNiftySellingPutData(BreezeConnect breezeConnect, int putEndStrike, String strFromDate,
			String strToDate, String[] bNSellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startPutStrike = putEndStrike - 900;

			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startPutStrike * CommonConstants.BankNiftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
						CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
						String.valueOf(startPutStrike));

				// System.out.println(nseEntity);

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					// System.out.println("Expiry : " + bNSellingExpiryDate[i] + " | Strike price: "
					// + startPutStrike
					// + " | Minimum Premium: " + Math.round(minimumPremium) + " | 2DLL: " +
					// Math.round(two2DLL)
					// + " | " + obito.get(0).getLow() + " | " + obito.get(1).getLow());

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						System.out.println("\n\nBANK NIFTY PUT OPTION SELLING");
						System.out.println("BANK NIFTY PE : " + startPutStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + Math.round(entry));
						System.out.println("Target   : " + Math.round(target));
						System.out.println("Stoploss : " + Math.round(stoploss));
						System.out.println("MSL      : " + Math.round(msl));
						System.out.println("TSL      : " + Math.round(tsl));
						// System.out.println(obito.get(0).getLow() + " " + obito.get(1).getLow() + " "
						// + (putEndStrike - 900) + " " + minimumPremium);

						return "";
					}
				} else {
					System.out.println("NO Data found for BANK NIFTY " + startPutStrike + " CE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startPutStrike = startPutStrike + 100;
			}
		}
		System.out.println("Not Found : NO NIFTY PE Call Today");
		return "";
	}

}
