package trading.api.model;

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
public class UserInput {

	private String sessionKey;
	private String todaysDate;
	private String fromDate;
	private String toDate;
	private String niftyFutExpiryDate;
	private String bNFutExpiryDate;
	
	private String niftySellingExpiryDate0;
	private String niftySellingExpiryDate1;
	private String niftySellingExpiryDate2;
	private String niftySellingExpiryDate3;
	 
	private String bNSellingExpiryDate0;
	private String bNSellingExpiryDate1;
	private String bNSellingExpiryDate2;
	private String bNSellingExpiryDate3;
}
