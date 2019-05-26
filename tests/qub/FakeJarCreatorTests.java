package qub;

public interface FakeJarCreatorTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(FakeJarCreator.class, () ->
        {
            JarCreatorTests.test(runner, FakeJarCreator::new);
        });
    }
}
