package tuttifrutti.cache;

import org.springframework.stereotype.Component;

import play.cache.Cache;
import tuttifrutti.models.enums.PowerUpType;
import tuttifrutti.utils.ConfigurationAccessor;

/**
 * @author rfanego
 */
@Component
public class RusCache {

	public static final String PREFIX = "RUS_";
	
	public void loadRus(PowerUpType powerUp) {
		Cache.set(PREFIX + powerUp.toString(), ConfigurationAccessor.i("rus.powerUp." + powerUp.toString()));
	}

	public int rusFor(PowerUpType powerUp){
		return (int)Cache.get(PREFIX + powerUp.toString());
	}
}
