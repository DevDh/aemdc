package com.headwire.aemdc.menu;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.headwire.aemdc.command.CommandMenu;
import com.headwire.aemdc.command.CreateFileCommand;
import com.headwire.aemdc.command.ReplacePlaceHoldersCommand;
import com.headwire.aemdc.companion.Constants;
import com.headwire.aemdc.companion.Resource;
import com.headwire.aemdc.util.ConfigUtil;


/**
 * Java Model creator
 *
 */
public class ModelRunner extends BasisRunner {

  private static final String HELP_FOLDER = "model";

  /**
   * Invoker
   */
  private final CommandMenu menu = new CommandMenu();
  private Resource resource;

  /**
   * Constructor
   *
   * @param resource
   *          - resource object
   * @throws IOException
   *           - IOException
   */
  public ModelRunner(final Resource resource) throws IOException {
    // Get Config Properties from config file
    final Properties configProps = ConfigUtil.getConfigProperties();

    resource.setSourceFolderPath(configProps.getProperty(Constants.CONFIGPROP_SOURCE_MODELS_FOLDER));
    resource.setTargetFolderPath(configProps.getProperty(Constants.CONFIGPROP_TARGET_MODELS_FOLDER));

    checkConfiguration(configProps, resource);

    // Set all other config properties in the resource
    setGlobalConfigProperties(configProps, resource);
    setJavaConfigProperties(configProps, resource);

    // Creates Invoker object, command object and configure them
    menu.setCommand("CreateFile", new CreateFileCommand(resource));
    menu.setCommand("ReplacePlaceHolders", new ReplacePlaceHoldersCommand(resource));
  }

  /**
   * Run commands
   *
   * @throws IOException
   */
  @Override
  public void run() throws IOException {
    // Invoker invokes command
    menu.runCommand("CreateFile");
    menu.runCommand("ReplacePlaceHolders");
  }

  @Override
  public String getHelpFolder() {
    return HELP_FOLDER;
  }

  @Override
  public String getSourceFolder() {
    return resource.getSourceFolderPath();
  }

  @Override
  public Collection<File> listAvailableTemplates(final File dir) {
    final Collection<File> fileList = FileUtils.listFiles(dir, new String[] { Constants.FILE_EXT_JAVA }, true);
    return fileList;
  }

}
