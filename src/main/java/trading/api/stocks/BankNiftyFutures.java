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
public class BankNiftyFutures {

	public void getBankNiftyFutData(BreezeConnect breezeConnect, String fromDate, String toDate, String bNFutExpiryDate)
			throws JsonMappingException, JsonProcessingException {

		String strFromDate = fromDate + "T" + CommonConstants.fromDateISOIndex;
		String strToDate = toDate + "T" + CommonConstants.toDateISOIndex;
		String expiryDate = bNFutExpiryDate + "T" + CommonConstants.toDateISOIndex;

		NseEntity nseEntity = breezeConnect.getHistoricalData(CommonConstants.DAY, strFromDate, strToDate,
				CommonConstants.BANKNIFTY, CommonConstants.NFO, CommonConstants.FUTURES, expiryDate, "", "");

		BankNiftyCalculations(nseEntity);
		System.out.println("-----------------------------");

	}

	private void BankNiftyCalculations(NseEntity nseEntity) {

		if (nseEntity.getObito().size() == 2) {
			List<NseValues> obito = nseEntity.getObito();

			// Step 3
			double two2DHH = (obito.get(0).getHigh() > obito.get(1).getHigh()) ? obito.get(0).getHigh()
					: obito.get(1).getHigh();
			double two2DLL = (obito.get(0).getLow() < obito.get(1).getLow()) ? obito.get(0).getLow()
					: obito.get(1).getLow();

			// Step 4
			double buyEntry = two2DHH * CommonConstants.BankNiftyFutBuyEntry;
			double buyTarget = buyEntry * CommonConstants.BankNiftyFutBuyTarget;
			double buyBeforeTargetStopLoss = Math.max((buyEntry * CommonConstants.BankNiftyFutSellTarget),
					(two2DLL * CommonConstants.BankNiftyFutSellEntry));
			double buyAfterTargetStopLoss = Math.max(buyEntry, (two2DLL * CommonConstants.BankNiftyFutSellEntry));

			double sellEntry = two2DLL * CommonConstants.BankNiftyFutSellEntry;
			double sellTarget = sellEntry * CommonConstants.BankNiftyFutSellTarget;
			double sellBeforeTargetStopLoss = Math.min((sellEntry * CommonConstants.BankNiftyFutBuyTarget),
					(two2DHH * CommonConstants.BankNiftyFutBuyEntry));
			double sellAfterTargetStopLoss = Math.min(sellEntry, (two2DHH * CommonConstants.BankNiftyFutBuyEntry));

			System.out.println("\n\nBANK NIFTY FUTURES");
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
