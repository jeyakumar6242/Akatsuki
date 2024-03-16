package trading.api.stocks;

import java.text.ParseException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import trading.api.constants.CommonConstants;
import trading.api.entity.NseEntity;
import trading.api.entity.NseValues;
import trading.api.utility.connection.BreezeConnect;

@Component
public class NiftyFutures {

	public String getNiftyFutData(BreezeConnect breezeConnect, String fromDate, String toDate,
			String niftyFutExpiryDate) throws ParseException, JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;
		String expiryDate = niftyFutExpiryDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.NIFTY, CommonConstants.NFO, CommonConstants.FUTURES, expiryDate, "", "");

		NiftyCalculations(nseEntity);
		return null;
	}
	
	private void NiftyCalculations(NseEntity nseEntity) {

		if (nseEntity.getObito().size() == 2) {
			List<NseValues> obito = nseEntity.getObito();

			// Step 3
			float two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
					: obito.get(1).getHigh();
			float two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
					: obito.get(1).getLow();

			// Step 4
			float buyEntry = two2DHH * CommonConstants.niftyFutBuyEntry;
			float buyTarget = buyEntry * CommonConstants.niftyFutBuyTarget;
			float buyBeforeTargetStopLoss = Math.max((buyEntry * CommonConstants.niftyFutSellTarget),
					(two2DLL * CommonConstants.niftyFutSellEntry));
			float buyAfterTargetStopLoss = Math.max(buyEntry, (two2DLL * CommonConstants.niftyFutSellEntry));

			float sellEntry = two2DLL * CommonConstants.niftyFutSellEntry;
			float sellTarget = sellEntry * CommonConstants.niftyFutSellTarget;
			float sellBeforeTargetStopLoss = Math.min((sellEntry * CommonConstants.niftyFutBuyTarget),
					(two2DHH * CommonConstants.niftyFutBuyEntry));
			float sellAfterTargetStopLoss = Math.min(sellEntry, (two2DHH * CommonConstants.niftyFutBuyEntry));

			System.out.println("\n\nFUTURES - NIFTY");
			System.out.println("Buy Entry  : " + Math.round(buyEntry));
			System.out.println("Buy Target : " + Math.round(buyTarget));
			System.out.println("StopLoss 1 : " + Math.round(buyBeforeTargetStopLoss));
			System.out.println("StopLoss 2 : " + Math.round(buyAfterTargetStopLoss));

			System.out.println("\n");
			System.out.println("Sell Entry  : " + Math.round(sellEntry));
			System.out.println("Sell Target : " + Math.round(sellTarget));
			System.out.println("StopLoss 1  : " + Math.round(sellBeforeTargetStopLoss));
			System.out.println("StopLoss 2  : " + Math.round(sellAfterTargetStopLoss));

		} else {
			System.out.println("Incorrect From and TO date. Please check.");
		}

	}
}
