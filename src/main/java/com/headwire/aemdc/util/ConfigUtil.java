package com.headwire.aemdc.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemdc.companion.Constants;


/**
 * Config Util
 *
 * @author Marat Saitov, 25.10.2016
 */
public class ConfigUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

  /**
   * Constructor
   */
  private ConfigUtil() {
  }

  /**
   * Get properties from configuration file
   *
   * @return configuration properties
   * @throws IOException
   *           - IOException
   */
  public static Properties getConfigProperties() throws IOException {

    Properties props = new Properties();
    try {
      props = PropsUtil.getProperties(Constants.CONFIG_PROPS_FILENAME);
    } catch (final IOException e) {
      LOG.error("Please create a configuration properties file [{}] in the root folder.",
          Constants.CONFIG_PROPS_FILENAME);
    }

    // replace path place holders
    if (!props.isEmpty()) {
      props = replacePathPlaceHolders(props);
    }
    return props;
  }

  /**
   * Check whether configurated source paths exist
   *
   * @return true - if all paths exist, false - otherwise
   * @throws IOException
   *           IOException
   */
  public static boolean checkConfiguration() throws IOException {
    boolean status = true;

    // Get Config Properties from config file
    final Properties configProps = getConfigProperties();
    if (!configProps.isEmpty()) {
      for (final String pathKey : Constants.SOURCE_PATHS) {
        final String path = configProps.getProperty(pathKey);
        if (StringUtils.isBlank(path)) {
          LOG.error("Please configurate the key [{}] in the configuration properties file [{}] in the root folder.",
              pathKey,
              Constants.CONFIG_PROPS_FILENAME);
          status = false;
        } else {
          final File file = new File(path);
          if (!file.exists()) {
            LOG.error("The path [{}] from configuration properties file [{}] doesn't exist.", path,
                Constants.CONFIG_PROPS_FILENAME);
            status = false;
          }
        }
      }
    } else {
      status = false;
    }

    return status;
  }

  /**
   * Get properties from default configuration file from resources folder
   *
   * @return configuration properties
   * @throws IOException
   *           - IOException
   */
  private static Properties getConfigPropertiesFromDefaultResource() throws IOException {
    final String configPropsFilePath = Constants.CONFIG_PROPS_FOLDER + "/" + Constants.CONFIG_PROPS_FILENAME;
    final Properties props = PropsUtil.getPropertiesFromContextClassLoader(configPropsFilePath);
    return props;
  }

  /**
   * Replace path placeholders in the configuration properties path values.
   *
   * @param configProps
   *          - configuration properties
   * @return initialized configuration properties
   */
  private static Properties replacePathPlaceHolders(final Properties configProps) {
    Properties newProps = new Properties();

    LOG.debug("Configuration properties path placeholders replacing... ");

    // source path placeholder values
    newProps = replacePathPlaceHolder(configProps, Constants.CONFIGPROP_SOURCE_FOLDER);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_SOURCE_UI_FOLDER);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_SOURCE_PROJECT_ROOT);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_SOURCE_JAVA_FOLDER);

    // target path placeholder values
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_TARGET_UI_FOLDER);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_TARGET_PROJECT_NAME);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_TARGET_PROJECT_ROOT);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_TARGET_JAVA_FOLDER);
    newProps = replacePathPlaceHolder(newProps, Constants.CONFIGPROP_TARGET_JAVA_PACKAGE);

    return newProps;
  }

  /**
   * Replace path placeholder in the configuration properties path values.
   *
   * @param configProps
   *          - configuration properties
   * @return initialized configuration properties
   */
  private static Properties replacePathPlaceHolder(final Properties configProps, final String placeHolderName) {
    final Properties newProps = new Properties();

    final String placeHolderValue = configProps.getProperty(placeHolderName);

    final Enumeration<?> e = configProps.propertyNames();
    while (e.hasMoreElements()) {
      final String key = (String) e.nextElement();
      String value = configProps.getProperty(key);

      LOG.debug("Original {}={}", key, value);
      value = value.replace("{{" + placeHolderName + "}}", placeHolderValue);
      LOG.debug("Replaced {}={}", key, value);

      // add with replaced path values
      newProps.put(key, value);
    }
    return newProps;
  }

  /**
   * Get properties from default configuration file as text
   *
   * @return default configuration properties as text
   * @throws IOException
   */
  public static String getDefaultConfigPropertiesAsText() throws IOException {
    final Properties props = getConfigPropertiesFromDefaultResource();
    final String configText = getConfigPropertiesAsText(props);
    return configText;
  }

  /**
   * Get properties from configuration file as text
   *
   * @param props
   *          - configuration properties
   * @return sorted configuration properties as text
   */
  public static String getConfigPropertiesAsText(final Properties props) {
    // get sorted list
    final List<String> sortedKeys = new ArrayList<String>();
    for (final String key : props.stringPropertyNames()) {
      sortedKeys.add(key);
    }
    Collections.sort(sortedKeys);

    final StringBuilder configText = new StringBuilder();
    for (final String key : sortedKeys) {
      final String value = props.getProperty(key);
      configText.append(key);
      configText.append("=");
      configText.append(value);
      configText.append("\n");
    }

    return configText.toString();
  }

  /**
   * Read extentions property from configuration file
   *
   * @param configProps
   *          - configuration properties
   * @return file extentions
   */
  public static String[] getConfigExtensions(final Properties configProps) {
    final String extentionsAsString = configProps.getProperty(Constants.CONFIGPROP_FILES_WITH_PLACEHOLDERS_EXTENSIONS);
    String[] extentions = Constants.FILES_PH_EXTENSIONS_DEFAULT;
    if (StringUtils.isNotBlank(extentionsAsString)) {
      extentions = extentionsAsString.split(",");
    }
    return extentions;
  }

}