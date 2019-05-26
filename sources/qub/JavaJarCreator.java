package qub;

public class JavaJarCreator extends JarCreator
{
    @Override
    public Result<File> createJarFile(Console console, boolean isVerbose)
    {
        return Result.create(() ->
        {
            final ProcessBuilder jar = console.getProcessBuilder("jar").await();
            jar.redirectOutput(console.getOutputByteWriteStream());
            jar.redirectError(console.getErrorByteWriteStream());

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
                console.writeLine(jar.getCommand());
            }

            jar.run().await();

            return jarFile;
        });
    }
}
