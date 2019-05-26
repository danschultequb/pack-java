package qub;

/**
 * A class that can be used to create jar files.
 */
public abstract class JarCreator
{
    /**
     * The folder that the jar operation will be run from.
     */
    private Folder baseFolder;
    /**
     * The manifest file that will be included in the jar file. If this is not provided, then no
     * manifest will be included in the jar file.
     */
    private File manifestFile;
    /**
     * The files that will be included in the jar file.
     */
    private Iterable<File> files;
    /**
     * The name of the created jar file.
     */
    private String jarName;

    /**
     * Set the folder that the jar operation will be run from.
     * @param baseFolder The folder that the jar operation will be run from.
     * @return This object for method chaining.
     */
    public JarCreator setBaseFolder(Folder baseFolder)
    {
        PreCondition.assertNotNull(baseFolder, "baseFolder");

        this.baseFolder = baseFolder;

        return this;
    }

    /**
     * Get the folder that the jar operation will be run from.
     * @return The folder that the jar operation will be run from.
     */
    public Folder getBaseFolder()
    {
        final Folder result = baseFolder;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the manifest file that will be packaged in the jar file.
     * @param manifestFile The manifest file that will be packaged in the jar file.
     * @return This object for method chaining.
     */
    public JarCreator setManifestFile(File manifestFile)
    {
        PreCondition.assertNotNull(manifestFile, "manifestFile");

        this.manifestFile = manifestFile;

        return this;
    }

    /**
     * Get the manifest file that will be packaged in the jar file, or null if no manifest file will
     * be added.
     * @return The manifest file that will be packaged in the jar file, or null if no manifest file
     * will be added.
     */
    public File getManifestFile()
    {
        return manifestFile;
    }

    /**
     * Set the files that will be included in the jar file.
     * @param files The files that will be included in the jar file.
     * @return This object for method chaining.
     */
    public JarCreator setFiles(Iterable<File> files)
    {
        PreCondition.assertNotNull(files, "files");

        this.files = files;

        return this;
    }

    /**
     * Get the files that will be included in the jar file.
     * @return The files that will be included in the jar file.
     */
    public Iterable<File> getFiles()
    {
        final Iterable<File> result = files;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the name of the jar file that will be created.
     * @param jarName The name of the jar file that will be created.
     */
    public JarCreator setJarName(String jarName)
    {
        PreCondition.assertNotNullAndNotEmpty(jarName, "jarName");

        this.jarName = jarName;

        return this;
    }

    /**
     * Get the name of the jar file that will be created.
     * @return The name of the jar file that will be created.
     */
    public String getJarName()
    {
        final String result = jarName;

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Create and return a reference to the jar file.
     * @param process The Process that will be used to run commands.
     * @param isVerbose Whether or not to show verbose logs.
     * @return The reference to the created jar file.
     */
    public abstract Result<File> createJarFile(Process process, boolean isVerbose);
}
