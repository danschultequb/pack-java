package qub;

public class JavaJarCreator extends JarCreator
{
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

            String jarArguments = "cf";
            final File manifestFile = getManifestFile();
            if (manifestFile != null)
            {
                jarArguments += 'm';
            }
            jar.addArgument(jarArguments);

            final File jarFile = baseFolder.getFile(getJarName() + ".jar").await();
            jar.addArgument(jarFile.getPath().toString());

            if (manifestFile != null)
            {
                jar.addArgument(manifestFile.relativeTo(baseFolder).toString());
            }

            jar.addArguments(getFiles()
                .map((File file) -> file.relativeTo(baseFolder).toString()));

            if (isVerbose)
            {
                process.getOutputCharacterWriteStream().writeLine(jar.getCommand()).await();
            }

            jar.run().await();

            return jarFile;
        });
    }
}
