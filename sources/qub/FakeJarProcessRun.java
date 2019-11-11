package qub;

public class FakeJarProcessRun extends FakeProcessRunDecorator<FakeJarProcessRun> implements JarArguments<FakeJarProcessRun>
{
    private FileSystem fileSystem;
    private Path manifestFilePath;
    private Path jarFilePath;
    private final List<Path> contentFilePaths;

    public FakeJarProcessRun()
    {
        super(new BasicFakeProcessRun(JarProcessBuilder.executablePath));

        this.contentFilePaths = List.create();
    }

    @Override
    public FakeJarProcessRun setWorkingFolder(Folder folder)
    {
        super.setWorkingFolder(folder);
        this.fileSystem = folder.getFileSystem();
        return this;
    }

    @Override
    public FakeJarProcessRun addJarFile(Path jarFilePath)
    {
        JarArguments.super.addJarFile(jarFilePath);
        this.jarFilePath = jarFilePath;
        return this;
    }

    @Override
    public FakeJarProcessRun addJarFile(File jarFile)
    {
        JarArguments.super.addJarFile(jarFile);
        this.fileSystem = jarFile.getFileSystem();
        return this;
    }

    @Override
    public FakeJarProcessRun addManifestFile(Path manifestFilePath)
    {
        this.manifestFilePath = manifestFilePath;
        JarArguments.super.addManifestFile(manifestFilePath);

        return this;
    }

    @Override
    public FakeJarProcessRun addManifestFile(File manifestFile)
    {
        JarArguments.super.addManifestFile(manifestFile);
        this.fileSystem = manifestFile.getFileSystem();
        return this;
    }

    @Override
    public FakeJarProcessRun addContentFilePath(Path contentFilePath)
    {
        PreCondition.assertNotNull(contentFilePath, "contentFilePath");

        contentFilePath = this.relativeToWorkingFolderPath(contentFilePath);
        this.contentFilePaths.add(contentFilePath);
        JarArguments.super.addContentFilePath(contentFilePath);

        return this;
    }

    public FakeJarProcessRun setFunctionAutomatically()
    {
        PreCondition.assertNotNull(this.getWorkingFolderPath(), "this.getWorkingFolderPath()");
        PreCondition.assertNotNull(this.jarFilePath, "this.jarFilePath");
        PreCondition.assertNotNull(this.fileSystem, "this.fileSystem");

        return this.setFunction(() ->
        {
            final Folder workingFolder = this.fileSystem.getFolder(this.getWorkingFolderPath()).await();
            final File jarFile = workingFolder.getFile(this.jarFilePath).await();
            try (final CharacterWriteStream jarFileStream = jarFile.getContentCharacterWriteStream().await())
            {
                if (this.manifestFilePath != null)
                {
                    jarFileStream.writeLine("Manifest File:").await();
                    jarFileStream.writeLine(this.manifestFilePath.toString());
                    jarFileStream.writeLine();
                }

                jarFileStream.writeLine("Content Files:").await();
                for (final Path contentFilePath : this.contentFilePaths)
                {
                    jarFileStream.writeLine(contentFilePath.toString()).await();
                }
            }
        });
    }
}
