package tuttifrutti.cache;

import static tuttifrutti.utils.ConfigurationAccessor.i;
import static tuttifrutti.utils.ConfigurationAccessor.s;

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
				+ i("rus.powerup." + powerUp.toString()));
		Logger.info(s("rus.powerup." + powerUp.toString()));
		Logger.info(s("elasticsearch.host"));
		Logger.info(i("powerup.loader.count") + "");
		Logger.info(i("rus.powerup.autocomplete") + "");
//		rus.powerup.autocomplete
		Cache.set(PREFIX + powerUp.toString(), ConfigurationAccessor.i("rus.powerup." + powerUp.toString()));
	}

	public int rusFor(PowerUpType powerUp){
		return (Integer)Cache.get(PREFIX + powerUp.toString());
	}
}
