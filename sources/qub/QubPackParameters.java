package qub;

/**
 * The parameters for the qub-pack application.
 */
public class QubPackParameters extends QubTestRunParameters
{
    private boolean packJson;
    private boolean parallelPack;

    /**
     * Create a new QubPackParameters object.
     * @param inputReadStream The ByteReadStream that input should be read from.
     * @param outputWriteStream The ByteWriteStream that output should be written to.
     * @param errorWriteStream The ByteWriteStream that errors should be written to.
     * @param folderToPack The folder that should have its tests run.
     * @param environmentVariables The environment variables of the running process.
     * @param processFactory The factory that will be used to create new processes.
     * @param defaultApplicationLauncher The object that will launch the default application for
     *                                   given files.
     */
    public QubPackParameters(CharacterToByteReadStream inputReadStream, CharacterToByteWriteStream outputWriteStream, CharacterToByteWriteStream errorWriteStream, Folder folderToPack, EnvironmentVariables environmentVariables, ProcessFactory processFactory, DefaultApplicationLauncher defaultApplicationLauncher, String jvmClassPath, TypeLoader typeLoader)
    {
        super(inputReadStream, outputWriteStream, errorWriteStream, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, QubPackParameters.getQubTestDataFolder(folderToPack, typeLoader), typeLoader);
    }

    private static Folder getQubTestDataFolder(Folder folderToPack, TypeLoader typeLoader)
    {
        PreCondition.assertNotNull(folderToPack, "folderToPack");
        PreCondition.assertNotNull(typeLoader, "typeLoader");

        final FileSystem fileSystem = folderToPack.getFileSystem();
        final Path path = typeLoader.getTypeContainerPath(QubTest.class).await();
        Folder projectVersionFolder;
        if (fileSystem.fileExists(path).await())
        {
            projectVersionFolder = fileSystem.getFile(path).await().getParentFolder().await();
        }
        else
        {
            projectVersionFolder = fileSystem.getFolder(path).await();
        }
        return QubProjectVersionFolder.get(projectVersionFolder)
            .getProjectDataFolder().await();
    }

    public QubPackParameters setPackJson(boolean packJson)
    {
        this.packJson = packJson;
        return this;
    }

    public boolean getPackJson()
    {
        return this.packJson;
    }

    /**
     * Set whether or not the jar files will be packaged in parallel.
     * @param parallelPack Whether or not the jar files will be packaged in parallel.
     * @return This object for method chaining.
     */
    public QubPackParameters setParallelPack(boolean parallelPack)
    {
        this.parallelPack = parallelPack;
        return this;
    }

    /**
     * Get whether or not the jar files will be packaged in parallel.
     * @return Whether or not the jar files will be packaged in parallel.
     */
    public boolean getParallelPack()
    {
        return this.parallelPack;
    }

    /**
     * Get the folder that should be packed.
     * @return The folder that should be packed.
     */
    public Folder getFolderToPack()
    {
        return this.getFolderToTest();
    }

    @Override
    public QubPackParameters setPattern(String pattern)
    {
        return (QubPackParameters)super.setPattern(pattern);
    }

    @Override
    public QubPackParameters setCoverage(Coverage coverage)
    {
        return (QubPackParameters)super.setCoverage(coverage);
    }

    @Override
    public QubPackParameters setTestJson(boolean testJson)
    {
        return (QubPackParameters)super.setTestJson(testJson);
    }

    @Override
    public QubPackParameters setJvmClassPath(String jvmClassPath)
    {
        return (QubPackParameters)super.setJvmClassPath(jvmClassPath);
    }

    @Override
    public QubPackParameters setProfiler(boolean profiler)
    {
        return (QubPackParameters)super.setProfiler(profiler);
    }

    @Override
    public QubPackParameters setWarnings(Warnings warnings)
    {
        return (QubPackParameters)super.setWarnings(warnings);
    }

    @Override
    public QubPackParameters setBuildJson(boolean buildJson)
    {
        return (QubPackParameters)super.setBuildJson(buildJson);
    }

    @Override
    public QubPackParameters setVerbose(VerboseCharacterToByteWriteStream verbose)
    {
        return (QubPackParameters)super.setVerbose(verbose);
    }

    public static boolean getPackJsonDefault()
    {
        return true;
    }

    public static boolean getParallelPackDefault()
    {
        return true;
    }
}
