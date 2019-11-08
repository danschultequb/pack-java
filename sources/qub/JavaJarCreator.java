package qub;

public class JavaJarCreator extends JarCreator
{
    /**
     * Get the arguments that will be passed to the jar command.
     * @return The arguments that will be passed to the jar command.
     */
    public List<String> getJarCommandArguments()
    {
        List<String> result = List.create();

        String jarArguments = "cf";
        final File manifestFile = getManifestFile();
        if (manifestFile != null)
        {
            jarArguments += 'm';
        }
        result.add(jarArguments);

        final Folder baseFolder = getBaseFolder();

        result.add(getJarFile().getPath().toString());

        if (manifestFile != null)
        {
            result.add(manifestFile.relativeTo(baseFolder).toString());
        }

        result.addAll(getFiles()
            .map((File file) -> file.relativeTo(baseFolder).toString()));

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    @Override
    public Result<File> createJarFile(ProcessFactory processFactory, ByteWriteStream output, ByteWriteStream error, VerboseCharacterWriteStream verbose)
    {
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(error, "error");
        PreCondition.assertNotNull(verbose, "verbose");

        return Result.create(() ->
        {
            final ProcessBuilder jar = processFactory.getProcessBuilder("jar").await();

            final Folder baseFolder = this.getBaseFolder();
            jar.setWorkingFolder(baseFolder);

            jar.addArguments(this.getJarCommandArguments());

            if (verbose.isVerbose())
            {
                jar.redirectOutput(output);
                jar.redirectError(error);
                verbose.writeLine("Running " + jar.getCommand()).await();
            }

            jar.run().await();

            return getJarFile();
        });
    }
}
