package qub;

public interface JarCreatorTests
{
    static void test(TestRunner runner, Function0<JarCreator> creator)
    {
        runner.testGroup(JarCreator.class, () ->
        {
            runner.testGroup("setBaseFolder(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("baseFolder cannot be null."),
                        () -> jarCreator.setBaseFolder(null));
                });

                runner.test("with non-existing folder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/i/dont/exist/").await();
                    test.assertFalse(baseFolder.exists().await());

                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setBaseFolder(baseFolder));
                    test.assertSame(baseFolder, jarCreator.getBaseFolder());
                });

                runner.test("with existing folder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.createFolder("/i/exist/").await();
                    test.assertTrue(baseFolder.exists().await());

                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setBaseFolder(baseFolder));
                    test.assertSame(baseFolder, jarCreator.getBaseFolder());
                });
            });

            runner.testGroup("getBaseFolder()", () ->
            {
                runner.test("when no baseFolder has been set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getBaseFolder());
                });
            });

            runner.testGroup("setManifestFile(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("manifestFile cannot be null."),
                        () -> jarCreator.setManifestFile(null));
                });

                runner.test("with non-existing file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final File manifestFile = fileSystem.getFile("/i/dont/exist").await();
                    test.assertFalse(manifestFile.exists().await());

                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setManifestFile(manifestFile));
                    test.assertSame(manifestFile, jarCreator.getManifestFile());
                });

                runner.test("with existing file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final File manifestFile = fileSystem.createFile("/i/exist").await();
                    test.assertTrue(manifestFile.exists().await());

                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setManifestFile(manifestFile));
                    test.assertSame(manifestFile, jarCreator.getManifestFile());
                });
            });

            runner.testGroup("setFiles(Iterable<File>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("files cannot be null."),
                        () -> jarCreator.setFiles(null));
                });

                runner.test("with empty", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    final Iterable<File> files = Iterable.create();
                    test.assertSame(jarCreator, jarCreator.setFiles(files));
                    test.assertSame(files, jarCreator.getFiles());
                });
            });

            runner.testGroup("getFiles()", () ->
            {
                runner.test("before files have been set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getFiles());
                });
            });

            runner.testGroup("setJarName(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("jarName cannot be null."),
                        () -> jarCreator.setJarName(null));
                });

                runner.test("with empty", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("jarName cannot be empty."),
                        () -> jarCreator.setJarName(""));
                });

                runner.test("with non-empty jarName without .jar extension", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setJarName("hello"));
                    test.assertEqual("hello", jarCreator.getJarName());
                });

                runner.test("with non-empty jarName with .jar extension", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertSame(jarCreator, jarCreator.setJarName("hello.jar"));
                    test.assertEqual("hello.jar", jarCreator.getJarName());
                });
            });

            runner.testGroup("getJarName()", () ->
            {
                runner.test("before jarName has been set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarName());
                });
            });

            runner.testGroup("createJarFile(Process,boolean)", () ->
            {
                runner.test("with null Process", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PreConditionFailure("process cannot be null."),
                        () -> jarCreator.createJarFile(null, false));
                });

                runner.test("with no baseFolder set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();
                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.createJarFile(test.getProcess(), false).await());
                });

                runner.test("with no jarName set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    jarCreator.setBaseFolder(fileSystem.getFolder("/base/folder/").await());

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.createJarFile(test.getProcess(), false).await());
                });

                runner.test("with no files set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    jarCreator.setBaseFolder(fileSystem.getFolder("/base/folder/").await());

                    jarCreator.setJarName("hello");

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.createJarFile(test.getProcess(), false).await());
                });

                runner.test("with manifestFile set but no files set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    jarCreator.setManifestFile(baseFolder.getFile("manifest.file").await());

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.createJarFile(test.getProcess(), false).await());
                });
            });

            runner.testGroup("getJarFile()", () ->
            {
                runner.test("when baseFolder has not been set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarFile());
                });

                runner.test("when jarName has not been set", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    test.assertThrows(new PostConditionFailure("result cannot be null."),
                        () -> jarCreator.getJarFile());
                });

                runner.test("when jarName does not end in .jar", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello");

                    final File jarFile = jarCreator.getJarFile();
                    test.assertNotNull(jarFile);
                    test.assertFalse(jarFile.exists().await());
                    test.assertEqual("/base/folder/hello.jar", jarFile.toString());
                });

                runner.test("when jarName ends in .jar", (Test test) ->
                {
                    final JarCreator jarCreator = creator.run();

                    final InMemoryFileSystem fileSystem = createFileSystem(test);
                    final Folder baseFolder = fileSystem.getFolder("/base/folder/").await();
                    jarCreator.setBaseFolder(baseFolder);

                    jarCreator.setJarName("hello.jar");

                    final File jarFile = jarCreator.getJarFile();
                    test.assertNotNull(jarFile);
                    test.assertFalse(jarFile.exists().await());
                    test.assertEqual("/base/folder/hello.jar.jar", jarFile.toString());
                });
            });
        });
    }

    static InMemoryFileSystem createFileSystem(Test test)
    {
        final InMemoryFileSystem result = new InMemoryFileSystem(test.getClock());
        result.createRoot("/").await();

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertTrue(result.rootExists("/").await(), "result.rootExists(\"/\").await()");

        return result;
    }
}
