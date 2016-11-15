package com.headwire.aemdc.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemdc.companion.Constants;


/**
 * Help Util
 *
 * @author Marat Saitov, 03.11.2016
 */
public class HelpUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HelpUtil.class);
  public static final String HELP_ROOT_FOLDER = "help";
  public static final String HELP_COMMON_FOLDER = "common";
  public static final String HELP_TEMPLATE_FOLDER = "template";
  public static final String HELP_COMPONENT_FOLDER = "component";
  public static final String HELP_OSGI_FOLDER = "osgi";
  public static final String HELP_EDITABLE_TEMPLATE_STRUCTURE_FOLDER = "confstr";
  public static final String HELP_MODEL_FOLDER = "model";
  public static final String HELP_SERVICE_FOLDER = "service";
  public static final String HELP_SERVLET_FOLDER = "servlet";
  public static final String HELP_FILE_START = "help-start.txt";
  public static final String HELP_FILE_OPTIONS = "help-options.txt";
  public static final String HELP_FILE_CONFIG = "help-config.txt";
  public static final String HELP_FILE_TYPE = "help-type.txt";
  public static final String HELP_FILE_NAME = "help-name.txt";
  public static final String HELP_FILE_TARGET_NAME = "help-targetname.txt";
  public static final String HELP_FILE_ARGS = "help-args.txt";

  /**
   * Constructor
   */
  private HelpUtil() {
  }

  /**
   * Shows help text.
   *
   * @throws IOException
   *           - IOException
   */
  public static void showHelp(final String[] args) throws IOException {
    System.out.print(getHelpText(args));
  }

  /**
   * Build help text from helper files.
   *
   * @return help text
   * @throws IOException
   *           - IOException
   */
  public static String getHelpText(final String[] args) throws IOException {
    String helpText = "";

    // no args or help
    if (args == null || args.length == 0 || (args.length == 1 && Constants.PARAM_HELP.equals(args[0]))) {
      helpText = getCompleteHelpText();

    } else if (Constants.PARAM_HELP.equals(args[0])) {
      String type = "";
      String name = "";

      if (args.length > 1) {
        type = args[1];
      }
      if (args.length > 2) {
        name = args[2];
      }
      helpText = getSpecificHelpText(type, name);

    } else {
      // not help and args.length < 3
      String type = "";
      String name = "";

      if (args.length > 0) {
        type = args[0];
      }
      if (args.length > 1) {
        name = args[1];
      }
      helpText = getSpecificHelpText(type, name);
    }

    // get complete help
    if (StringUtils.isBlank(helpText)) {
      helpText = getCompleteHelpText();
    }

    return helpText;
  }

  /**
   * Get complete help text from helper files.
   *
   * @return help text
   * @throws IOException
   *           - IOException
   */
  public static String getCompleteHelpText() throws IOException {
    final StringBuilder helpText = new StringBuilder();

    // get complete help
    helpText.append(getTextFromFile(HELP_FILE_START));
    helpText.append(getTextFromFile(HELP_FILE_OPTIONS));
    helpText.append(getTextFromFile(HELP_FILE_CONFIG));
    helpText.append(getTextFromFile(HELP_FILE_TYPE));
    helpText.append(getTextFromFile(HELP_COMMON_FOLDER + "/" + HELP_FILE_TYPE));

    // name option
    helpText.append(getTextFromFile(HELP_FILE_NAME));
    helpText.append(getTextFromFile(HELP_COMMON_FOLDER + "/" + HELP_FILE_NAME));

    // targetname option
    helpText.append(getTextFromFile(HELP_FILE_TARGET_NAME));
    helpText.append(getTextFromFile(HELP_COMMON_FOLDER + "/" + HELP_FILE_TARGET_NAME));

    // args option
    helpText.append(getTextFromFile(HELP_FILE_ARGS));
    helpText.append(getTextFromFile(HELP_COMMON_FOLDER + "/" + HELP_FILE_ARGS));

    return helpText.toString();
  }

  /**
   * Get template type specific help text.
   *
   * @param type
   *          - template type
   * @param name
   *          - template name
   * @return type specific help text
   * @throws IOException
   *           - IOException
   */
  public static String getSpecificHelpText(final String type, final String name) throws IOException {
    final StringBuilder helpText = new StringBuilder();

    // config type
    if (Constants.TYPE_CONFIG_PROPS.equals(type)) {
      // show current config properties
      helpText.append(ConfigUtil.getConfigPropertiesAsText());

    } else if (StringUtils.isNotBlank(type) && StringUtils.isBlank(name)) {
      // if only <type>
      if (isExistingType(type)) {
        helpText.append(getTextFromFile(HELP_FILE_START));
        helpText.append(getTextFromFile(HELP_FILE_NAME));
        helpText.append(getTextFromFile(getTypeHelpFolder(type) + "/" + HELP_FILE_NAME));
        helpText.append(getTextFromFile(HELP_FILE_TARGET_NAME));
        helpText.append(getTextFromFile(getTypeHelpFolder(type) + "/" + HELP_FILE_TARGET_NAME));
        helpText.append(getTextFromFile(HELP_FILE_ARGS));
        helpText.append(getTextFromFile(getTypeHelpFolder(type) + "/" + HELP_FILE_ARGS));
        // get all available templates
        helpText.append(getTemplates(type));
      }

    } else if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
      // if <type> + <name>
      if (isExistingType(type)) {
        helpText.append(getTextFromFile(HELP_FILE_START));
        helpText.append(getTextFromFile(HELP_FILE_TARGET_NAME));
        helpText.append(getTextFromFile(getTypeHelpFolder(type) + "/" + HELP_FILE_TARGET_NAME));
        helpText.append(getTextFromFile(HELP_FILE_ARGS));
        helpText.append(getTextFromFile(getTypeHelpFolder(type) + "/" + HELP_FILE_ARGS));
        // get all placeholders
        helpText.append(getPlaceHolders(type, name));
      }
    }

    return helpText.toString();
  }

  /**
   * Is existing type
   *
   * @param type
   *          - template type
   * @return true if type exists
   */
  public static boolean isExistingType(final String type) {
    if (Constants.TYPE_APPS_UI_LIST.contains(type) || Constants.TYPE_CORE_LIST.contains(type)
        || Constants.TYPE_CONF_UI_LIST.contains(type)) {
      return true;
    }
    return false;
  }

  /**
   * Read help text from helper file.
   *
   * @param fileName
   *          - help file name
   * @return help text
   * @throws IOException
   *           - IOException
   */
  private static String getTextFromFile(final String fileName) throws IOException {
    final InputStream in = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(HELP_ROOT_FOLDER + "/" + fileName);
    final StringWriter writer = new StringWriter();
    final StringBuilder helpText = new StringBuilder();

    try {
      IOUtils.copy(in, writer, Constants.ENCODING);
      helpText.append(writer.toString());
    } catch (final IOException e) {
      LOG.error("Sorry, can't show you help text from file {}", fileName);
      throw new IOException(e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (final IOException e) {
          LOG.error("Sorry, unable to close input stream from help file {}", fileName);
          throw new IOException(e);
        }
      }
    }
    helpText.append("\n");
    return helpText.toString();
  }

  /**
   * Get list of all existing placeholders in the template.
   *
   * @param type
   *          - template type
   * @param name
   *          - template source name
   * @return list of placeholders
   * @throws IOException
   *           - IOException
   */
  private static String getPlaceHolders(final String type, final String name) throws IOException {
    // Get Config Properties from config file
    final Properties configProps = ConfigUtil.getConfigProperties();
    final String templateSrcPath = ConfigUtil.getTypeSourceFolder(configProps, type) + "/" + name;
    final File dir = new File(templateSrcPath);

    final StringBuilder placeHolders = new StringBuilder();

    if (!dir.exists()) {
      placeHolders.append("Can't get place holders. Directory/file " + templateSrcPath + " doesn't exist.");
    } else {
      placeHolders.append("Found next placeholders: \n");

      if (dir.isDirectory()) {
        // get files list recursive only with predefined extentions
        final String[] extentions = ConfigUtil.getConfigExtensions(configProps);
        final Collection<File> fileList = FileUtils.listFiles(dir, extentions, true);
        final Iterator<File> iter = fileList.iterator();
        while (iter.hasNext()) {
          final File nextFile = iter.next();
          // find place holders
          placeHolders.append(getPlaceHolders(nextFile));
        }
      } else {
        // find place holders
        placeHolders.append(getPlaceHolders(dir));
      }
    }

    return placeHolders.toString();
  }

  /**
   * Get all placeholders from the file
   *
   * @param file
   *          - file to find placeholders there
   * @return
   * @throws IOException
   *           - IOException
   */
  private static String getPlaceHolders(final File file) throws IOException {
    final StringBuilder placeHolders = new StringBuilder();
    try {
      final String text = FileUtils.readFileToString(file, Constants.ENCODING);
      // find placeholders
      final List<String> phList = TextReplacer.findTextPlaceHolders(text);
      final Iterator<String> iter = phList.iterator();
      while (iter.hasNext()) {
        // add offset for help
        placeHolders.append("    ");
        placeHolders.append(iter.next());
        placeHolders.append("\n");
      }
    } catch (final IOException e) {
      LOG.error("Can't get place holders from {}", file);
      throw new IOException(e);
    }
    return placeHolders.toString();
  }

  /**
   * Get list of all existing templates for type.
   *
   * @param type
   *          - template type
   * @return list of all existing templates
   * @throws IOException
   *           - IOException
   */
  private static String getTemplates(final String type) throws IOException {
    final StringBuilder templs = new StringBuilder();

    // Get Config Properties from config file
    final Properties configProps = ConfigUtil.getConfigProperties();
    final String searchPath = ConfigUtil.getTypeSourceFolder(configProps, type);
    final File dir = new File(searchPath);

    if (!dir.exists()) {
      templs.append("Can't get available templates. Directory " + searchPath + " doesn't exist.");
    } else {
      if (dir.isDirectory()) {
        templs.append("available names: \n");

        // find available templates
        final Collection<File> fileList = findTemplates(dir, type);
        final Iterator<File> iter = fileList.iterator();
        while (iter.hasNext()) {
          final File nextFile = iter.next();
          templs.append("    ");
          templs.append(getTemplateName(dir, nextFile));
          templs.append("\n");
          LOG.debug("Found: {}", nextFile);
        }
      } else {
        templs.append("Can't get available templates. The " + searchPath + " isn't directory.");
      }
    }

    return templs.toString();
  }

  /**
   * Get template name incl. subfolders.
   *
   * @param dir
   *          - templates directory
   * @param nextFile
   *          - template file under the directory
   * @return template name
   */
  private static String getTemplateName(final File dir, final File templateFile) {
    // get template name incl. subfolders, for ex. "impl/SampleServiceImpl.java"
    String name = StringUtils.substringAfter(templateFile.getPath(), dir.getPath());

    // convert to unix path format
    name = name.replace("\\", "/");

    // cut first slash
    if (name.indexOf("/") == 0) {
      name = name.substring(1);
    }
    return name;
  }

  /**
   * Get list of all existing templates for type.
   *
   * @param type
   *          - template type
   * @return list of all existing templates
   * @throws IOException
   *           - IOException
   */
  private static Collection<File> findTemplates(final File dir, final String type) {
    Collection<File> fileList = new ArrayList<File>();

    switch (type) {
      case Constants.TYPE_TEMPLATE:
      case Constants.TYPE_TEMPLATE_FULL:
      case Constants.TYPE_COMPONENT:
      case Constants.TYPE_COMPONENT_FULL:
      case Constants.TYPE_EDITABLE_TEMPLATE_STRUCTURE:
        for (final File file : FileUtils.listFilesAndDirs(dir, FalseFileFilter.INSTANCE,
            DirectoryFileFilter.INSTANCE)) {
          // get only root directories
          final String name = getTemplateName(dir, file);
          if (StringUtils.isNotBlank(name) && !name.contains("/")) {
            fileList.add(file);
          }
        }
        break;
      case Constants.TYPE_OSGI:
        fileList = FileUtils.listFiles(dir, new String[] { Constants.FILE_EXT_XML }, false);
        break;
      case Constants.TYPE_MODEL:
      case Constants.TYPE_SERVICE:
      case Constants.TYPE_SERVLET:
        fileList = FileUtils.listFiles(dir, new String[] { Constants.FILE_EXT_JAVA }, true);
        break;
      default:
        throw new IllegalStateException("Unknown <type> argument: " + type);
    }
    return fileList;
  }

  /**
   * Get type help text folder.
   *
   * @param type
   *          - template type
   * @return type help folder
   */
  public static String getTypeHelpFolder(final String type) {
    String typeHelpFolder = "";
    switch (type) {
      case Constants.TYPE_TEMPLATE:
      case Constants.TYPE_TEMPLATE_FULL:
        typeHelpFolder = HELP_TEMPLATE_FOLDER;
        break;
      case Constants.TYPE_COMPONENT:
      case Constants.TYPE_COMPONENT_FULL:
        typeHelpFolder = HELP_COMPONENT_FOLDER;
        break;
      case Constants.TYPE_OSGI:
        typeHelpFolder = HELP_OSGI_FOLDER;
        break;
      case Constants.TYPE_EDITABLE_TEMPLATE_STRUCTURE:
        typeHelpFolder = HELP_EDITABLE_TEMPLATE_STRUCTURE_FOLDER;
        break;
      case Constants.TYPE_MODEL:
        typeHelpFolder = HELP_MODEL_FOLDER;
        break;
      case Constants.TYPE_SERVICE:
        typeHelpFolder = HELP_SERVICE_FOLDER;
        break;
      case Constants.TYPE_SERVLET:
        typeHelpFolder = HELP_SERVLET_FOLDER;
        break;
      default:
        throw new IllegalStateException("Unknown <type> argument: " + type);
    }
    return typeHelpFolder;
  }
}