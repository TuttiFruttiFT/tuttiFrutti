package tuttifrutti.spring;

import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchMode.Q;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import tuttifrutti.jobs.ExpiredRoundCheckerJob;

@Configuration
@ComponentScan({ "tuttifrutti" })
public class CommonSpringConfiguration {
	
	@Bean
	public ExpiredRoundCheckerJob expiredNormalRoundCheckerJob(){
		return new ExpiredRoundCheckerJob(N);
	}
	
	@Bean
	public ExpiredRoundCheckerJob expiredQuickRoundCheckerJob(){
		return new ExpiredRoundCheckerJob(Q);
	}
}
