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

	private String todaysDate;
	private String fromDate;
	private String toDate;
	private String niftyFutExpiryDate;
	private String bNFutExpiryDate;
}
