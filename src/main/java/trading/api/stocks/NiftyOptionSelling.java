package trading.api.stocks;

import java.text.DecimalFormat;
import java.text.ParseException;
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
public class NiftyOptionSelling {

	@Autowired
	OutputResult outputResult;

	private static final DecimalFormat decfor = new DecimalFormat("0.00");

	public String getNiftyOptionSellingData(BreezeConnect breezeConnect, String fromDate, String toDate,
			String[] niftySellingExpiryDate) throws ParseException, JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.NIFTY, CommonConstants.NSE, "", "", "", "");

		NiftyOptionSellingCalculations(breezeConnect, nseEntity, strFromDate, strToDate, niftySellingExpiryDate);
		return null;
	}

	private void NiftyOptionSellingCalculations(BreezeConnect breezeConnect, NseEntity nseEntity, String strFromDate,
			String strToDate, String[] niftySellingExpiryDate) throws JsonMappingException, JsonProcessingException {
		if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
			List<NseValues> obito = nseEntity.getObito();

			double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
					: obito.get(1).getHigh();
			double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
					: obito.get(1).getLow();

			int callBuffer = (int) (two2DLL * CommonConstants.niftyCallStrikeFactor);
			int putBuffer = (int) (two2DHH * CommonConstants.niftyPutStrikeFactor);

			int callEndStrike = (callBuffer / 50) * 50; // Round Down
			int putEndStrike = ((putBuffer / 50) + 1) * 50; // Round Up

			getNiftySellingCallData(breezeConnect, callEndStrike, strFromDate, strToDate, niftySellingExpiryDate);
			getNiftySellingPutData(breezeConnect, putEndStrike, strFromDate, strToDate, niftySellingExpiryDate);

		} else {
			System.out.println("Incorrect From and TO date. Please check.");
		}
	}

	private String getNiftySellingCallData(BreezeConnect breezeConnect, int callEndStrike, String strFromDate,
			String strToDate, String[] niftySellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startCallStrike = callEndStrike + 450;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startCallStrike * CommonConstants.niftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
						CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
						String.valueOf(startCallStrike));

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nNIFTY CE : " + startCallStrike;
						s = s + "\nExpiry   : " + niftySellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setSellingNiftyCeEntry(s);

						System.out.println("\n\nOPTION SELLING - NIFTY CE");
						System.out.println("NIFTY CE : " + startCallStrike);
						System.out.println("Expiry   : " + niftySellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}

				} else {
					System.out.println(
							"NO Data found for NIFTY " + startCallStrike + " CE | Expiry " + niftySellingExpiryDate[i]);
				}
				startCallStrike = startCallStrike - 50;
			}
		}
		System.out.println("Not Found : NO NIFTY CE Call Today");
		return "";
	}

	private String getNiftySellingPutData(BreezeConnect breezeConnect, int putEndStrike, String strFromDate,
			String strToDate, String[] niftySellingExpiryDate) throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startPutStrike = putEndStrike - 450;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startPutStrike * CommonConstants.niftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
						CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
						String.valueOf(startPutStrike));

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nNIFTY PE : " + startPutStrike;
						s = s + "\nExpiry   : " + niftySellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setSellingNiftyPeEntry(s);

						System.out.println("\n\nOPTION SELLING - NIFTY PE");
						System.out.println("NIFTY CE : " + startPutStrike);
						System.out.println("Expiry   : " + niftySellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));

						return "";
					}
				} else {
					System.out.println(
							"NO Data found for NIFTY " + startPutStrike + " PE | Expiry " + niftySellingExpiryDate[i]);
				}
				startPutStrike = startPutStrike + 50;
			}
		}
		System.out.println("Not Found : NO NIFTY PE Call Today");
		return "";
	}

//########################################## GAP UP DOWN #######################################################################

	public void getNiftyGapUpDown(BreezeConnect breezeConnect, String strTodaysDate, String fromDate, String toDate,
			String[] niftySellingExpiryDate) throws JsonMappingException, JsonProcessingException {
		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate, strTodaysDate,
				CommonConstants.NIFTY, CommonConstants.NSE, "", "", "", "");

		NiftyOptionSellingGapUpDownCalculations(breezeConnect, nseEntity, strTodaysDate, strFromDate, strToDate,
				niftySellingExpiryDate);
	}

	private void NiftyOptionSellingGapUpDownCalculations(BreezeConnect breezeConnect, NseEntity nseEntity,
			String strTodaysDate, String strFromDate, String strToDate, String[] niftySellingExpiryDate)
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

			int callBuffer = (int) (low * CommonConstants.niftyFutSellEntry); // Gap down
			int putBuffer = (int) (high * CommonConstants.niftyFutBuyEntry); // Gap Up

			int callEndStrike = (callBuffer / 50) * 50; // Round Down
			int putEndStrike = ((putBuffer / 50) + 1) * 50; // Round Up

			getNiftySellingCallGapDownData(breezeConnect, callEndStrike, strTodaysDate, strFromDate, strToDate,
					niftySellingExpiryDate);
			getNiftySellingGapUpPutData(breezeConnect, putEndStrike, strTodaysDate, strFromDate, strToDate,
					niftySellingExpiryDate);

		} else {
			System.out.println("Incorrect From and TO date. Please check.");
		}

	}

	private String getNiftySellingGapUpPutData(BreezeConnect breezeConnect, int putEndStrike, String strTodaysDate,
			String strFromDate, String strToDate, String[] niftySellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startPutStrike = putEndStrike - 450;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startPutStrike * CommonConstants.niftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate,
						strTodaysDate, CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
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
								strToDate, CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
								niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.PUT,
								String.valueOf(startPutStrike));

						List<NseValues> pain = nseEntity1.getObito();

						double two2DHH = (pain.get(0).getHigh() > pain.get(1).getHigh()) ? pain.get(0).getHigh()
								: pain.get(1).getHigh();

						double entry = low * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nNIFTY PE : " + startPutStrike;
						s = s + "\nExpiry   : " + niftySellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setGapUpSellingNiftyPeEntry(s);

						System.out.println("\n\nOPTION SELLING - NIFTY GAP UP PE");
						System.out.println("NIFTY CE : " + startPutStrike);
						System.out.println("Expiry   : " + niftySellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));
						
						return "";
					}
				} else {
					System.out.println(
							"NO Data found for NIFTY " + startPutStrike + " PE | Expiry " + niftySellingExpiryDate[i]);
				}
				startPutStrike = startPutStrike + 50;
			}
		}
		System.out.println("Not Found : NO NIFTY GAP UP PE Call Today");
		return "";

	}

	private String getNiftySellingCallGapDownData(BreezeConnect breezeConnect, int callEndStrike, String strTodaysDate,
			String strFromDate, String strToDate, String[] niftySellingExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		for (int i = 0; i <= 3; i++) {

			int startCallStrike = callEndStrike + 450;
			for (int strikeIncrement = 1; strikeIncrement <= 10; strikeIncrement++) {

				double minimumPremium = startCallStrike * CommonConstants.niftyMinimumPremium;

				NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.fiveMinute, strTodaysDate,
						strTodaysDate, CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
						niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
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
								strToDate, CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.OPTIONS,
								niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex, CommonConstants.CALL,
								String.valueOf(startCallStrike));

						List<NseValues> pain = nseEntity1.getObito();

						double two2DHH = (pain.get(0).getHigh() > pain.get(1).getHigh()) ? pain.get(0).getHigh()
								: pain.get(1).getHigh();

						double entry = low * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						String s = "\nNIFTY CE : " + startCallStrike;
						s = s + "\nExpiry   : " + niftySellingExpiryDate[i];
						s = s + "\nEntry    : " + decfor.format(entry);
						s = s + "\nTarget   : " + decfor.format(target);
						s = s + "\nStoploss : " + decfor.format(stoploss);
						s = s + "\nMSL      : " + decfor.format(msl);
						s = s + "\nTSL      : " + decfor.format(tsl);
						outputResult.setGapDownSellingNiftyCeEntry(s);;

						System.out.println("\n\nOPTION SELLING - NIFTY GAP DOWN CE");
						System.out.println("NIFTY CE : " + startCallStrike);
						System.out.println("Expiry   : " + niftySellingExpiryDate[i]);
						System.out.println("Entry    : " + decfor.format(entry));
						System.out.println("Target   : " + decfor.format(target));
						System.out.println("Stoploss : " + decfor.format(stoploss));
						System.out.println("MSL      : " + decfor.format(msl));
						System.out.println("TSL      : " + decfor.format(tsl));
						
						return "";
					}

				} else {
					System.out.println(
							"NO Data found for NIFTY " + startCallStrike + " CE | Expiry " + niftySellingExpiryDate[i]);
				}
				startCallStrike = startCallStrike - 50;
			}
		}
		System.out.println("Not Found : NO NIFTY GAP DOWN CE Call Today");
		return "";

	}

}
