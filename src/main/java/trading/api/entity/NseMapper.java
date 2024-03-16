package trading.api.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Entity
@Getter
@Setter
@ToString
public class NseMapper {

	public void mappingNseEntity(String response) {

		NseEntity nseEntity = new NseEntity();
		nseEntity.setError(response);
		nseEntity.setStatus(response);
		nseEntity.setObito(null);
	}

}
