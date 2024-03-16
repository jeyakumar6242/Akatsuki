package trading.api.entity;

import java.util.List;

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
public class NseEntity {

	@JsonProperty("Status")
	public String status;
	
	@JsonProperty("Error")
	public String error;
	
	@JsonProperty("Success")
	public List<NseValues> obito;
}
