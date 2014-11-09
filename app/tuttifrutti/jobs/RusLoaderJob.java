package tuttifrutti.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.cache.RusCache;
import tuttifrutti.models.enums.PowerUpType;

/**
 * @author rfanego
 */
@Component
public class RusLoaderJob implements Runnable {

	@Autowired
	RusCache cache;
	
	@Override
	public void run() {
		Logger.info("Starting RusLoaderJob");
		for(PowerUpType powerUp : PowerUpType.values()){
			cache.loadRus(powerUp);
		}
		Logger.info("Finishing RusLoaderJob");
	}
}
