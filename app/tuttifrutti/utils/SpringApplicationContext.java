package tuttifrutti.utils;

import static lombok.AccessLevel.PRIVATE;
import static tuttifrutti.spring.RuntimeEnvironment.currentRuntimeEnvironment;
import lombok.NoArgsConstructor;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import tuttifrutti.spring.CommonSpringConfiguration;

/**
 * @author rfanego
 */
@NoArgsConstructor(access = PRIVATE)
public class SpringApplicationContext {
	private static ConfigurableApplicationContext ctx;

	public static void initialize() {
		try {
			ctx = new AnnotationConfigApplicationContext(CommonSpringConfiguration.class);
			ctx.getEnvironment().setActiveProfiles(currentRuntimeEnvironment().name());
			ctx.refresh();
			if (ctx == null) {
				throw new IllegalStateException("application context could not be initialized properly");
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("application context could not be initialized properly", e);
		}
	}

	public static <T> T getBean(Class<T> beanClass) {
		return ctx.getBean(beanClass);
	}

	public static <T> T getBeanNamed(String beanName, Class<T> beanClass) {
		return ctx.getBean(beanName, beanClass);
	}

	public static void close() {
		if (ctx != null) {
			ctx.close();
			ctx = null;
		}
	}
}
