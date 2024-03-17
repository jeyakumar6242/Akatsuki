package trading.api.model;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Component
public class OutputResult {

	private String futuresNiftyBuyEntry;
	private String futuresNiftySellEntry;
	private String futuresBankNiftyBuyEntry;
	private String futuresBankNiftySellEntry;

	private String sellingNiftyCeEntry;
	private String sellingNiftyPeEntry;
	private String sellingBankNiftyCeEntry;
	private String sellingBankNiftyPeEntry;
	
	private String gapDownSellingNiftyCeEntry;
	private String gapUpSellingNiftyPeEntry;
	private String gapDownSellingBankNiftyCeEntry;
	private String gapUpSellingBankNiftyPeEntry;
	
}
