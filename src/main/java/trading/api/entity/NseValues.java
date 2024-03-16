package trading.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NseValues {

	@JsonProperty("datetime")
	public String date;
	
	@JsonProperty("stock_code")
	public String stockName;
	
	@JsonProperty("exchange_code")
	public String exchangeName;
	
	@JsonProperty("product_type")
	public String productType;
	
	@JsonProperty("expiry_date")
	public String expiryDate;
	
	@JsonProperty("right")
	public String callOrPut;
	
	@JsonProperty("strike_price")
	public int strikePrice;
	
	@JsonProperty("open")
	public float open;
	
	@JsonProperty("high")
	public float high;
	
	@JsonProperty("low")
	public float low;
	
	@JsonProperty("close")
	public float close;
	
	@JsonProperty("volume")
	public float volume;
	
	@JsonProperty("open_interest")
	public double openInterest;
	
	@JsonProperty("count")
	public int count;

}
