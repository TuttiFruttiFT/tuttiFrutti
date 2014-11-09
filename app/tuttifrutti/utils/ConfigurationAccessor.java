package tuttifrutti.utils;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.FileSystemResourceLoader;

import play.Configuration;

import com.typesafe.config.ConfigFactory;

/**
 * This class intends to be a short(er) cut for accessing externalized properties than what Play!
 * provides.<br>
 * It is worth noting that using {@link Configuration#root()} is preferred over using
 * {@link ConfigFactory#load()} as the former includes properties overridden by tests while the
 * latter only loads stuff from application*.conf files.
 * 
 */
@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class ConfigurationAccessor {

	/**
	 * Convenience method for accessing the value of a String property, configured in one or many
	 * applicationX.conf file(s).
	 */
	public static String s(String propertyName) {
		return Configuration.root().getString(propertyName);
	}

	/**
	 * Same as {@link #s(String)} but parsing the retrieved value as an Integer.
	 */
	public static Integer i(String propertyName) {
		return Configuration.root().getInt(propertyName);
	}

	/**
	 * Same as {@link #s(String)} but parsing the retrieved value as an Boolean.
	 */
	public static Boolean b(String propertyName) {
		return Configuration.root().getBoolean(propertyName);
	}

	/**
	 * Convenience method for accessing the value of a configuration property, configured in a
	 * properties file
	 */
	public static String s(String propertyName, Properties props) {
		return props.getProperty(propertyName);
	}

	/**
	 * Same as above but with a default value in case the property is not defined
	 */
	public static String s(String propertyName, String defaultValue, Properties props) {
		return props.getProperty(propertyName, defaultValue);
	}

	@SneakyThrows(IOException.class)
	public static Properties loadConfigPropertiesFrom(String filePath) {
		InputStream is = null;
		val path = (filePath.startsWith("/") ? "file:" : "") + filePath;
		try {
			is = new FileSystemResourceLoader().getResource(path).getInputStream();
		} catch (IOException e) {
			log.error("Could not load configuration properties file from " + filePath, e);
		}
		if (is == null) {
			throw new IllegalStateException(format("properties file %s not found in this project", filePath));
		}
		Properties properties = new Properties();
		properties.load(is);

		return properties;
	}
}
