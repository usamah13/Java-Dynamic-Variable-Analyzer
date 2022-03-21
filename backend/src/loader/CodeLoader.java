package loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class CodeLoader {
    private static final String MODIFIED_AST_FILE_PATH = "backend/test/modifiedast/";
    private static final String MODIFIED_FILES_DIRECTORY = "backend/test/modifiedast";
    private static final String MODIFIED_VARIABLE_LOGGER_FILE_PATH = MODIFIED_FILES_DIRECTORY + "/VariableLogger.java";
    private static final String MODIFIED_LINE_INFO_FILE_PATH = MODIFIED_FILES_DIRECTORY + "/LineInfo.java";
    private static final String MODIFIED_VARIABLE_REF_LOGGER_FILE_PATH = MODIFIED_FILES_DIRECTORY +
            "/VariableReferenceLogger.java";

    public static void run(String classname) throws Exception {

        File sourceFile = new File(MODIFIED_AST_FILE_PATH + classname + ".java");
        File loggerFile = new File(MODIFIED_VARIABLE_LOGGER_FILE_PATH);
        File lineInfoFile = new File(MODIFIED_LINE_INFO_FILE_PATH);
        File refLoggerFile = new File(MODIFIED_VARIABLE_REF_LOGGER_FILE_PATH);

        StringWriter sw = new StringWriter();

        // compile the source file
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        File parentDirectory = sourceFile.getParentFile();
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(parentDirectory));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(loggerFile, lineInfoFile, refLoggerFile, sourceFile));
        boolean success = compiler.getTask(sw, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();

        // Check if code compiled successfully
        if (!success) {
            throw new Exception(sw.toString());
        }

        // load the compiled class
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { parentDirectory.toURI().toURL() });
        Class<?> modifiedClass = classLoader.loadClass("modifiedast." + classname);

        // call main on the loaded class
        Method method = modifiedClass.getDeclaredMethod("main", String[].class);
        method.invoke(modifiedClass.getDeclaredConstructor().newInstance(), (Object) null);

        sourceFile.delete();
    }
}
