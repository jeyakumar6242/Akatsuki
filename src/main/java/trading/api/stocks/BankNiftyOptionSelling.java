package trading.api.stocks;

import java.text.DecimalFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import trading.api.constants.CommonConstants;
import trading.api.entity.NseEntity;
import trading.api.entity.NseValues;
import trading.api.model.OutputResult;
import trading.api.utility.connection.BreezeConnect;

@Component
public class BankNiftyOptionSelling {

	@Autowired
	OutputResult outputResult;

	private static final DecimalFormat decfor = new DecimalFormat("0.00");

	public void getBankNiftyOptionSellingData(BreezeConnect breezeConnect, String fromDate, String toDate,
			String[] bNSellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.BANKNIFTY, CommonConstants.NSE, "", "", "", "");

		BankNiftyOptionSellingCalculations(breezeConnect, nseEntity, strFromDate, strToDate, bNSellingExpiryDate);
	}

	private void BankNiftyOptionSellingCalculations(BreezeConnect breezeConnect, NseEntity nseEntity,
			String strFromDate, String strToDate, String[] bNSellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
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

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					if (two2DLL > minimumPremium) {
						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nBANK NIFTY CE : " + startCallStrike;
						s = s + "\nExpiry   : " + bNSellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setSellingBankNiftyCeEntry(s);

						System.out.println("\n\nOPTION SELLING - BANK NIFTY CE");
						System.out.println("NIFTY CE : " + startCallStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}

				} else {
					System.out.println("NO Data found for BANK NIFTY " + startCallStrike + " CE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startCallStrike = startCallStrike - 100;
			}
		}
		System.out.println("Not Found : NO BANK NIFTY CE Call Today");
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

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nBANK NIFTY PE : " + startPutStrike;
						s = s + "\nExpiry   : " + bNSellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setSellingBankNiftyPeEntry(s);

						System.out.println("\n\nOPTION SELLING - BANK NIFTY PE");
						System.out.println("NIFTY CE : " + startPutStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}
				} else {
					System.out.println("NO Data found for BANK NIFTY " + startPutStrike + " PE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startPutStrike = startPutStrike + 100;
			}
		}
		System.out.println("Not Found : NO BANK NIFTY PE Call Today");
		return "";
	}

//################################################## BANK NIFTY GAP UP/DOWN ##########################################################

	public void getBnGapUpDown(BreezeConnect breezeConnect, String strTodaysDate, String fromDate, String toDate,
			String[] bNSellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate, strTodaysDate,
				CommonConstants.BANKNIFTY, CommonConstants.NSE, "", "", "", "");

		BankNiftyOptionSellingGapUpDownCalculations(breezeConnect, nseEntity, strTodaysDate, strFromDate, strToDate,
				bNSellingExpiryDate);
	}

	private void BankNiftyOptionSellingGapUpDownCalculations(BreezeConnect breezeConnect, NseEntity nseEntity,
			String strTodaysDate, String strFromDate, String strToDate, String[] bNSellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		if (nseEntity != null && nseEntity.getObito() != null) {
			List<NseValues> obito = nseEntity.getObito();
			double high = obito.get(0).getHigh();
			double low = obito.get(0).getLow();

			if (high < obito.get(1).getHigh()) {
				high = obito.get(1).getHigh();
			}
			if (high < obito.get(2).getHigh()) {
				high = obito.get(2).getHigh();
			}

			if (low < obito.get(1).getLow()) {
				low = obito.get(1).getLow();
			}
			if (low < obito.get(2).getLow()) {
				low = obito.get(2).getLow();
			}
			int callBuffer = (int) (low * CommonConstants.BankNiftyFutSellEntry);
			int putBuffer = (int) (high * CommonConstants.BankNiftyFutBuyEntry);

			int callEndStrike = (callBuffer / 100) * 100; // Round Down
			int putEndStrike = ((putBuffer / 100) + 1) * 100; // Round Up

			getBankNiftySellingGapDownCallData(breezeConnect, callEndStrike, strTodaysDate, strFromDate, strToDate,
					bNSellingExpiryDate);
			getBankNiftySellingGapUpPutData(breezeConnect, putEndStrike, strTodaysDate, strFromDate, strToDate,
					bNSellingExpiryDate);

		} else {
			System.out.println("Incorrect From and TO date. Please check.");
		}

	}

	private String getBankNiftySellingGapUpPutData(BreezeConnect breezeConnect, int putEndStrike, String strTodaysDate,
			String strFromDate, String strToDate, String[] bNSellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startPutStrike = putEndStrike - 900;

			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startPutStrike * CommonConstants.BankNiftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate,
						strTodaysDate, CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
						String.valueOf(startPutStrike));

				if (nseEntity != null && nseEntity.getObito() != null) {
					List<NseValues> obito = nseEntity.getObito();

					double low = obito.get(0).getLow();

					if (low > obito.get(1).getLow()) {
						low = obito.get(1).getLow();
					}
					if (low > obito.get(2).getLow()) {
						low = obito.get(2).getLow();
					}

					if (low > minimumPremium) {

						NseEntity nseEntity1 = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate,
								strToDate, CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
								bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
								String.valueOf(startPutStrike));

						List<NseValues> pain = nseEntity1.getObito();

						double two2DHH = (pain.get(0).getHigh() > pain.get(1).getHigh()) ? pain.get(0).getHigh()
								: pain.get(1).getHigh();

						double entry = low * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nBANK NIFTY PE : " + startPutStrike;
						s = s + "\nExpiry   : " + bNSellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setGapUpSellingBankNiftyPeEntry(s);

						System.out.println("\n\nOPTION SELLING - BANK NIFTY GAP UP PE");
						System.out.println("NIFTY CE : " + startPutStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}
				} else {
					System.out.println("NO Data found for BANK NIFTY " + startPutStrike + " PE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startPutStrike = startPutStrike + 100;
			}
		}
		System.out.println("Not Found : NO BANK NIFTY GAP UP PE Call Today");
		return "";
	}

	private String getBankNiftySellingGapDownCallData(BreezeConnect breezeConnect, int callEndStrike,
			String strTodaysDate, String strFromDate, String strToDate, String[] bNSellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startCallStrike = callEndStrike + 900;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startCallStrike * CommonConstants.BankNiftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate,
						strTodaysDate, CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
						String.valueOf(startCallStrike));

				if (nseEntity != null && nseEntity.getObito() != null) {
					List<NseValues> obito = nseEntity.getObito();

					double low = obito.get(0).getLow();

					if (low > obito.get(1).getLow()) {
						low = obito.get(1).getLow();
					}
					if (low > obito.get(2).getLow()) {
						low = obito.get(2).getLow();
					}

					if (low > minimumPremium) {

						NseEntity nseEntity1 = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate,
								strToDate, CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
								bNSellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
								String.valueOf(startCallStrike));

						List<NseValues> pain = nseEntity1.getObito();

						double two2DHH = (pain.get(0).getHigh() > pain.get(1).getHigh()) ? pain.get(0).getHigh()
								: pain.get(1).getHigh();

						double entry = low * CommonConstants.BankNiftyEntry;
						double target = entry * CommonConstants.BankNiftyTarget;
						double msl = entry * CommonConstants.BankNiftyMSL;
						double tsl = two2DHH * CommonConstants.BankNiftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nBANK NIFTY CE : " + startCallStrike;
						s = s + "\nExpiry   : " + bNSellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setGapDownSellingBankNiftyCeEntry(s);

						System.out.println("\n\nOPTION SELLING - BANK NIFTY GAP DOWN CE");
						System.out.println("NIFTY CE : " + startCallStrike);
						System.out.println("Expiry   : " + bNSellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}

				} else {
					System.out.println("NO Data found for BANK NIFTY " + startCallStrike + " CE | Expiry "
							+ bNSellingExpiryDate[i]);
				}
				startCallStrike = startCallStrike - 100;
			}
		}
		System.out.println("Not Found : NO BANK NIFTY GAP DOWN CE Call Today");
		return "";
	}

}
