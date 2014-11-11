package tuttifrutti.cache;

import org.springframework.stereotype.Component;

import play.Logger;
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
		Logger.info("Loading Rus Cache " + PREFIX + powerUp.toString() + " value "
				+ ConfigurationAccessor.i("rus.powerup." + powerUp.toString()));
		Logger.info(ConfigurationAccessor.s("rus.powerup." + powerUp.toString()));
		Cache.set(PREFIX + powerUp.toString(), ConfigurationAccessor.i("rus.powerup." + powerUp.toString()));
	}

	public int rusFor(PowerUpType powerUp){
		return (Integer)Cache.get(PREFIX + powerUp.toString());
	}
}
