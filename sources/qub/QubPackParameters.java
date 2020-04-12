package qub;

/**
 * The parameters for the qub-pack application.
 */
public class QubPackParameters extends QubTestParameters
{
    private boolean packJson;

    /**
     * Create a new QubPackParameters object.
     * @param outputWriteStream The ByteWriteStream that output should be written to.
     * @param errorWriteStream The ByteWriteStream that errors should be written to.
     * @param folderToPack The folder that should have its tests run.
     * @param environmentVariables The environment variables of the running process.
     * @param processFactory The factory that will be used to create new processes.
     * @param defaultApplicationLauncher The object that will launch the default application for
     *                                   given files.
     */
    public QubPackParameters(CharacterToByteWriteStream outputWriteStream, CharacterToByteWriteStream errorWriteStream, Folder folderToPack, EnvironmentVariables environmentVariables, ProcessFactory processFactory, DefaultApplicationLauncher defaultApplicationLauncher, String jvmClassPath)
    {
        super(outputWriteStream, errorWriteStream, folderToPack, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);
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
    public QubPackParameters setVerbose(VerboseCharacterWriteStream verbose)
    {
        return (QubPackParameters)super.setVerbose(verbose);
    }

    public static boolean getPackJsonDefault()
    {
        return true;
    }
}
