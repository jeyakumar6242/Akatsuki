package trading.api.stocks;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import trading.api.constants.CommonConstants;
import trading.api.entity.NseEntity;
import trading.api.entity.NseValues;
import trading.api.utility.connection.BreezeConnect;

@Component
public class NiftyOptionSelling {
	public String getNiftyOptionSellingData(BreezeConnect breezeConnect, String fromDate, String toDate,
			String[] niftySellingExpiryDate) throws ParseException, JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.NIFTY, CommonConstants.NSE, "", "", "", "");

		NiftyOptionSellingCalculations(breezeConnect, nseEntity, strFromDate, strToDate, niftySellingExpiryDate);

		System.out.println("-----------------------------");
		return null;
	}

	private void NiftyOptionSellingCalculations(BreezeConnect breezeConnect, NseEntity nseEntity, String strFromDate,
			String strToDate, String[] niftySellingExpiryDate) throws JsonMappingException, JsonProcessingException {
		if (nseEntity.getObito().size() == 2) {
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

				// System.out.println(nseEntity);

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					// System.out.println("Expiry : " +expiryDate+ " | Strike price:
					// "+startCallStrike + " | Minimum Premium: " + minimumPremium+ " | 2DLL: "+
					// two2DLL);

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						System.out.println("\n\nNIFTY CALL OPTION SELLING");
						System.out.println("NIFTY CE " + startCallStrike);
						System.out.println(
								"Expiry   : " + niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex);
						System.out.println("Entry    : " + Math.round(entry));
						System.out.println("Target   : " + Math.round(target));
						System.out.println("Stoploss : " + Math.round(stoploss));
						System.out.println("MSL      : " + Math.round(msl));
						System.out.println("TSL      : " + Math.round(tsl));
						System.out.println(obito.get(0).getLow() + "     " + obito.get(1).getLow() + "    "
								+ (callEndStrike + 450) + "   " + minimumPremium);

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

				// System.out.println(nseEntity);

				if (nseEntity != null && nseEntity.getObito() != null && nseEntity.getObito().size() == 2) {
					List<NseValues> obito = nseEntity.getObito();

					double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
							: obito.get(1).getLow();

					// System.out.println("Expiry : " +expiryDate+ " | Strike price:
					// "+startPutStrike + " | Minimum Premium: " + Math.round(minimumPremium)+ " |
					// 2DLL: "+ Math.round(two2DLL) );

					if (two2DLL > minimumPremium) {

						double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
								: obito.get(1).getHigh();

						double entry = two2DLL * CommonConstants.niftyEntry;
						double target = entry * CommonConstants.niftyTarget;
						double msl = entry * CommonConstants.niftyMSL;
						double tsl = two2DHH * CommonConstants.niftyTSL;

						double stoploss = (msl < tsl) ? msl : tsl;

						System.out.println("\n\nNIFTY PUT OPTION SELLING");
						System.out.println("NIFTY PE " + startPutStrike);
						System.out.println(
								"Expiry   : " + niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex);
						System.out.println("Entry    : " + Math.round(entry));
						System.out.println("Target   : " + Math.round(target));
						System.out.println("Stoploss : " + Math.round(stoploss));
						System.out.println("MSL      : " + Math.round(msl));
						System.out.println("TSL      : " + Math.round(tsl));
						System.out.println(obito.get(0).getLow() + "     " + obito.get(1).getLow() + "    "
								+ (putEndStrike - 450) + "   " + minimumPremium);

						return "";
					}
				} else {
					System.out.println(
							"NO Data found for NIFTY " + startPutStrike + " CE | Expiry " + niftySellingExpiryDate[i]);
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

		System.out.println("-----------------------------");
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

						System.out.println("\n\nNIFTY GAP UP PUT OPTION SELLING");
						System.out.println("NIFTY PE " + startPutStrike);
						System.out.println(
								"Expiry   : " + niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex);
					/*	System.out.println("Entry    : " + Math.round(entry));
						System.out.println("Target   : " + Math.round(target));
						System.out.println("Stoploss : " + Math.round(stoploss));
						System.out.println("MSL      : " + Math.round(msl));
						System.out.println("TSL      : " + Math.round(tsl)); */
						
						System.out.println("Entry    : " + entry);
						System.out.println("Target   : " + target);
						System.out.println("Stoploss : " + stoploss);
						System.out.println("MSL      : " + msl);
						System.out.println("TSL      : " + tsl);

						return "";
					}
				} else {
					System.out.println(
							"NO Data found for NIFTY " + startPutStrike + " CE | Expiry " + niftySellingExpiryDate[i]);
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

				// System.out.println(nseEntity);

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

						System.out.println("\n\nNIFTY GAP DOWN CALL OPTION SELLING");
						System.out.println("NIFTY CE " + startCallStrike);
						System.out.println(
								"Expiry   : " + niftySellingExpiryDate[i] + "T" + CommonConstants.toDateISOIndex);
						System.out.println("Entry    : " + entry);
						System.out.println("Target   : " + target);
						System.out.println("Stoploss : " + stoploss);
						System.out.println("MSL      : " + msl);
						System.out.println("TSL      : " + tsl);
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
