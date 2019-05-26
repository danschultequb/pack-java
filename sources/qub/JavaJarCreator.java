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
    public Result<File> createJarFile(Process process, boolean isVerbose)
    {
        PreCondition.assertNotNull(process, "process");

        return Result.create(() ->
        {
            final ProcessBuilder jar = process.getProcessBuilder("jar").await();
            jar.redirectOutput(process.getOutputByteWriteStream());
            jar.redirectError(process.getErrorByteWriteStream());

            final Folder baseFolder = getBaseFolder();
            jar.setWorkingFolder(baseFolder);

            jar.addArguments(getJarCommandArguments());

            if (isVerbose)
            {
                process.getOutputCharacterWriteStream().writeLine(jar.getCommand()).await();
            }

            jar.run().await();

            return getJarFile();
        });
    }
}
