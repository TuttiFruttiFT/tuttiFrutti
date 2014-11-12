package tuttifrutti.cache;

import static tuttifrutti.utils.ConfigurationAccessor.i;

import org.springframework.stereotype.Component;

import play.cache.Cache;
import tuttifrutti.models.enums.PowerUpType;

/**
 * @author rfanego
 */
@Component
public class RusCache {

	public static final String PREFIX = "RUS_";
	
	public void loadRus(PowerUpType powerUp) {
		Cache.set(PREFIX + powerUp.toString(), i("rus.powerup." + powerUp.toString()));
	}

	public int rusFor(PowerUpType powerUp){
		return (Integer)Cache.get(PREFIX + powerUp.toString());
	}
}
