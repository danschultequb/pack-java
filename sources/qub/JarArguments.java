package qub;

public interface JarArguments<T>
{
    /**
     * Get the path to the folder that this ProcessBuilder will run the executable in.
     * @return The path to the folder that this ProcessBuilder will run the executable in.
     */
    Path getWorkingFolderPath();

    /**
     * Add the provided arguments to the list of arguments that will be provided to the executable
     * when this ProcessBuilder is run.
     * @param arguments The arguments to add.
     * @return This object for method chaining.
     */
    T addArguments(String... arguments);

    /**
     * Add the --create argument.
     * @return This object for method chaining.
     */
    default T addCreate()
    {
        return this.addArguments("--create");
    }

    /**
     * Add the --file argument.
     * @param jarFilePath The path to the jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    default T addJarFile(String jarFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(jarFilePath, "jarFilePath");

        return this.addJarFile(Path.parse(jarFilePath));
    }

    /**
     * Add the --file argument.
     * @param jarFilePath The path to the jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    default T addJarFile(Path jarFilePath)
    {
        PreCondition.assertNotNull(jarFilePath, "jarFilePath");

        jarFilePath = this.relativeToWorkingFolderPath(jarFilePath);
        return this.addArguments("--file=" + jarFilePath);
    }

    /**
     * Add the --file argument.
     * @param jarFile The jar file where the created file should be placed.
     * @return This object for method chaining.
     */
    default T addJarFile(File jarFile)
    {
        PreCondition.assertNotNull(jarFile, "jarFile");

        return this.addJarFile(jarFile.getPath());
    }

    /**
     * Add the --manifest argument.
     * @param manifestFilePath The path to the manifest file.
     * @return This object for method chaining.
     */
    default T addManifestFile(String manifestFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(manifestFilePath, "manifestFilePath");

        return this.addManifestFile(Path.parse(manifestFilePath));
    }

    /**
     * Add the --manifest argument.
     * @param manifestFilePath The path to the manifest file.
     * @return This object for method chaining.
     */
    default T addManifestFile(Path manifestFilePath)
    {
        PreCondition.assertNotNull(manifestFilePath, "manifestFilePath");

        return this.addArguments("--manifest=" + manifestFilePath);
    }

    /**
     * Add the --manifest argument.
     * @param manifestFile The manifest file.
     * @return This object for method chaining.
     */
    default T addManifestFile(File manifestFile)
    {
        PreCondition.assertNotNull(manifestFile, "manifestFile");

        return this.addManifestFile(manifestFile.getPath());
    }

    default T addContentFilePathStrings(Iterable<String> contentFilePathStrings)
    {
        PreCondition.assertNotNullAndNotEmpty(contentFilePathStrings, "contentFilePathStrings");

        return this.addContentFilePaths(contentFilePathStrings.map(Path::parse));
    }

    default T addContentFilePaths(Iterable<Path> contentFilePaths)
    {
        PreCondition.assertNotNull(contentFilePaths, "contentFilePaths");

        T result = null;
        for (final Path contentFilePath : contentFilePaths)
        {
            result = this.addContentFilePath(contentFilePath);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    default T addContentFiles(Iterable<File> contentFiles)
    {
        PreCondition.assertNotNullAndNotEmpty(contentFiles, "contentFiles");

        return this.addContentFilePaths(contentFiles.map(File::getPath));
    }

    default T addContentFilePath(String contentFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(contentFilePath, "contentFilePath");

        return this.addContentFilePath(Path.parse(contentFilePath));
    }

    default T addContentFilePath(Path contentFilePath)
    {
        PreCondition.assertNotNull(contentFilePath, "contentFilePath");

        contentFilePath = this.relativeToWorkingFolderPath(contentFilePath);
        return this.addArguments(contentFilePath.toString());
    }

    default Path relativeToWorkingFolderPath(Path path)
    {
        PreCondition.assertNotNull(path, "path");

        Path result = path;
        if (result.isRooted())
        {
            final Path workingFolderPath = this.getWorkingFolderPath();
            if (workingFolderPath != null)
            {
                result = result.relativeTo(workingFolderPath);
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
