package qub;

public interface JavaJarCreatorTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaJarCreator.class, () ->
        {
            JarCreatorTests.test(runner, JavaJarCreator::new);
        });
    }
}
