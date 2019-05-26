package qub;

public interface QubPackTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubPack.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("args cannot be null."),
                        () -> QubPack.main((String[])null));
                });
            });

            runner.testGroup("main(Console)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(new PreConditionFailure("console cannot be null."),
                        () -> main((Console)null));
                });
            });
        });
    }

    static void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final QubBuild build = new QubBuild();
        build.setJavaCompiler(new FakeJavaCompiler());
        build.setJarCreator(new FakeJarCreator());

        final QubTest test = new QubTest();
        test.setJavaRunner(new FakeJavaRunner());
        test.setQubBuild(build);

        final QubPack pack = new QubPack();
        pack.setQubTest(test);

        pack.main(console);
    }
}
