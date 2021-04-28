package qub;

/**
 * A ProcessBuilder that is specific to the javac application.
 */
public class JarProcessBuilder extends ProcessBuilderDecorator<JarProcessBuilder> implements JarArguments<JarProcessBuilder>
{
    public static final String executablePathString = "jar";
    public static final Path executablePath = Path.parse(JarProcessBuilder.executablePathString);

    private JarProcessBuilder(ProcessBuilder processBuilder)
    {
        super(processBuilder);
    }

    /**
     * Get a JarProcessBuilder from the provided Process.
     * @param process The Process to get the JarProcessBuilder from.
     * @return The JarProcessBuilder.
     */
    public static Result<JarProcessBuilder> get(RealDesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return JarProcessBuilder.get(process.getProcessFactory());
    }

    /**
     * Get a JarProcessBuilder from the provided ProcessFactory.
     * @param processFactory The ProcessFactory to get the JarProcessBuilder from.
     * @return The JarProcessBuilder.
     */
    public static Result<JarProcessBuilder> get(ProcessFactory processFactory)
    {
        PreCondition.assertNotNull(processFactory, "processFactory");

        return Result.create(() ->
        {
            return new JarProcessBuilder(processFactory.getProcessBuilder(JarProcessBuilder.executablePathString).await());
        });
    }
}
