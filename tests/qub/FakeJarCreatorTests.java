package qub;

public interface FakeJarCreatorTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(FakeJarCreator.class, () ->
        {
            JarCreatorTests.test(runner, FakeJarCreator::new);

            runner.testGroup("createJarFile(Process,boolean)", () ->
            {
                runner.test("with empty files set", (Test test) ->
                {
                    final FakeJarCreator jarCreator = new FakeJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setFiles(Iterable.create());

                    final ProcessFactory processFactory = test.getProcess().getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(false, output.asCharacterReadStream());

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    test.assertNotNull(jarFile);
                    test.assertTrue(jarFile.exists().await());
                    test.assertEqual(0, jarFile.getContents().await().length);
                });

                runner.test("with manifestFile set and empty files set", (Test test) ->
                {
                    final FakeJarCreator jarCreator = new FakeJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setManifestFile(baseFolder.getFile("manifest.file").await());

                    jarCreator.setFiles(Iterable.create());

                    final ProcessFactory processFactory = test.getProcess().getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(false, output.asCharacterReadStream());

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    test.assertNotNull(jarFile);
                    test.assertTrue(jarFile.exists().await());
                    test.assertEqual(
                        Iterable.create(
                            "Manifest file:",
                            "manifest.file",
                            ""
                        ),
                        Strings.getLines(jarFile.getContentsAsString().await()));
                });

                runner.test("with non-empty files set", (Test test) ->
                {
                    final FakeJarCreator jarCreator = new FakeJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setFiles(Iterable.create(
                        baseFolder.getFile("src/code.java").await(),
                        baseFolder.getFile("src/otherCode.java").await()
                    ));

                    final ProcessFactory processFactory = test.getProcess().getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(false, output.asCharacterReadStream());

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    test.assertNotNull(jarFile);
                    test.assertTrue(jarFile.exists().await());
                    test.assertEqual(
                        Iterable.create(
                            "Files:",
                            "src/code.java",
                            "src/otherCode.java",
                            ""
                        ),
                        Strings.getLines(jarFile.getContentsAsString().await()));
                });

                runner.test("with manifestFile set and empty files set", (Test test) ->
                {
                    final FakeJarCreator jarCreator = new FakeJarCreator();

                    final InMemoryFileSystem fileSystem = JarCreatorTests.createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setManifestFile(baseFolder.getFile("manifest.file").await());

                    jarCreator.setFiles(Iterable.create(
                        baseFolder.getFile("src/code.java").await(),
                        baseFolder.getFile("src/otherCode.java").await()
                    ));

                    final ProcessFactory processFactory = test.getProcess().getProcessFactory();
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final VerboseCharacterWriteStream verbose = new VerboseCharacterWriteStream(false, output.asCharacterReadStream());

                    final File jarFile = jarCreator.createJarFile(processFactory, output, error, verbose).await();
                    test.assertNotNull(jarFile);
                    test.assertTrue(jarFile.exists().await());
                    test.assertEqual(
                        Iterable.create(
                            "Manifest file:",
                            "manifest.file",
                            "",
                            "Files:",
                            "src/code.java",
                            "src/otherCode.java",
                            ""
                        ),
                        Strings.getLines(jarFile.getContentsAsString().await()));
                });
            });
        });
    }
}
