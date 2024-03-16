package trading.api.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CommonConstants {

	// Intervals
	public static final String DAY = "1Day";
	public static final String fiveMinute = "5minute";

	// Exchange Code
	public static final String NSE = "NSE";
	public static final String BSE = "BSE";
	public static final String NFO = "NFO";

	// Exchange Code
	public static final String FUTURES = "futures";
	public static final String OPTIONS = "options";
	public static final String CASH = "cash";
	public static final String FUTUREPLUS = "futureplus";

	// Right
	public static final String CALL = "call";
	public static final String PUT = "put";
	public static final String OTHERS = "others";

	// Indices
	public static final String NIFTY = "NIFTY";
	public static final String BANKNIFTY = "CNXBAN";

	// Exchange Code
	public static final String MONDAY = "MONDAY";
	public static final String TUESDAY = "TUESDAY";
	public static final String WEDNESDAY = "WEDNESDAY";
	public static final String THURSDAY = "THURSDAY";
	public static final String FRIDAY = "FRIDAY";
	public static final String SATURDAY = "SATURDAY";
	public static final String SUNDAY = "SUNDAY";

	public static final String fromDateISOIndex = "03:30:00.000Z";
	public static final String toDateISOIndex = "10:00:00.000Z";
	public static final String todaysFromDateISOIndex = "03:44:00.000Z";
	public static final String todaysToDateISOIndex = "04:01:00.000Z";

	// Formula

	// ***************** Nifty Futures
	// Buy Entry | 2DHH*(1+0.125%)
	public static final float niftyFutBuyEntry = (float) 1.00125;
	// Buy Target | Entry*(1+1.25%)
	public static final float niftyFutBuyTarget = (float) 1.0125;
	// Sell Entry | 2DLL*(1-0.125%)
	public static final float niftyFutSellEntry = (float) 0.99875;
	// Sell Target | Entry*(1-1.25%)
	public static final float niftyFutSellTarget = (float) 0.9875;

	// ##################################### BANK NIFTY FUTURES ########################################
	// Buy Entry | 2DHH*(1+0.15%)
	public static final float BankNiftyFutBuyEntry = (float) 1.0015;
	// Buy Target | Entry*(1+1.5%)
	public static final float BankNiftyFutBuyTarget = (float) 1.015;
	// Sell Entry | 2DLL*(1-0.15%)
	public static final float BankNiftyFutSellEntry = (float) 0.9985;
	// Sell Target | Entry*(1-1.5%)
	public static final float BankNiftyFutSellTarget = (float) 0.985;

	// #################################### Nifty Option Selling ######################################
	// Put Strike factor 2DHH*(1+0.15%)
	public static final double niftyPutStrikeFactor = 1.0015;
	// Call Strike factor 2DLL*(1-0.15%)
	public static final double niftyCallStrikeFactor = 0.9985;
	// Nifty Minimum Premium Strike price*0.85%
	public static final double niftyMinimumPremium = 0.0085;
	// Nifty Entry 2DLL*(1-10%)
	public static final double niftyEntry = 0.9;
	// Nifty Target Entry*(1-75%)
	public static final double niftyTarget = 0.25;
	// NIfty MSL Entry*(1+75%)
	public static final double niftyMSL = 1.75;
	// NIFTY TSL 2DHH*(1+10%)
	public static final double niftyTSL = 1.1;

	
	//########################## BANK NIFTY OPTION SELLING ##########################
	// Put Strike factor 2DHH*(1+0.15%)
	public static final double BankNiftyPutStrikeFactor = 1.0015;
	// Call Strike factor 2DLL*(1-0.15%)
	public static final double BankNiftyCallStrikeFactor = 0.9985;
	// Nifty Minimum Premium Strike price*1.1%
	public static final double BankNiftyMinimumPremium = 0.011;
	// Nifty Entry 2DLL*(1-10%)
	public static final double BankNiftyEntry = 0.9;
	// Nifty Target Entry*(1-75%)
	public static final double BankNiftyTarget = 0.25;
	// NIfty MSL Entry*(1+75%)
	public static final double BankNiftyMSL = 1.75;
	// NIFTY TSL 2DHH*(1+10%)
	public static final double BankNiftyTSL = 1.1;

}