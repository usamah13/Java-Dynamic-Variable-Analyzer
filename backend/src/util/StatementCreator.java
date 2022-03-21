package util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.Statement;

public final class StatementCreator {

    public static Statement logVariable(String name, String enclosingMethod, String enclosingClass, String value,
                                        int uniqueIdentifier) {
        if (value == null) {
            value = "\"uninitialized\"";
        }
        return log(value, name, enclosingMethod, enclosingClass, uniqueIdentifier);
    }

    public static Statement evaluateVarDeclarationWithoutInitializerStatement(String name, String enclosingMethod,
                                                                              String enclosingClass, int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableReferenceLogger.evaluateVarDeclarationWithoutInitializer(\""
                + name + "\", \""
                + enclosingMethod + "\", \""
                + enclosingClass + "\", "
                + uniqueNum + ");");
    }

    private static Statement log(String value, String name, String enclosingMethod, String enclosingClass,
                                 int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableLogger.log("
                + value + ", \""
                + name + "\", \""
                + enclosingMethod + "\", \""
                + enclosingClass + "\", "
                + uniqueNum + ");");
    }

    public static Statement evaluateAssignmentStatement(String name, String enclosingMethod, String enclosingClass,
                                                        int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableReferenceLogger.evaluateAssignment("
                + formatVariableReferenceLoggerMethodCall(name, enclosingMethod, enclosingClass, uniqueNum));
    }

    public static Statement evaluateVarDeclarationStatement(String name, String enclosingMethod,
                                                            String enclosingClass, int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableReferenceLogger.evaluateVarDeclaration("
                + formatVariableReferenceLoggerMethodCall(name, enclosingMethod, enclosingClass, uniqueNum));
    }

    public static Statement checkBaseAndNestedObjectsStatement(String name, String enclosingMethod,
                                                               String enclosingClass, int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableReferenceLogger.checkBaseAndNestedObjects("
                + formatVariableReferenceLoggerMethodCall(name, enclosingMethod, enclosingClass, uniqueNum));
    }

    public static Statement evaluateForLoopVarDeclarationStatement(String name, String enclosingMethod,
                                                                   String enclosingClass, int uniqueNum) {
        return StaticJavaParser.parseStatement("VariableReferenceLogger.evaluateForLoopVarDeclaration("
                + formatVariableReferenceLoggerMethodCall(name, enclosingMethod, enclosingClass, uniqueNum));
    }

    private static String formatVariableReferenceLoggerMethodCall(String name, String enclosingMethod,
                                                                  String enclosingClass, int uniqueNum) {
        return name + ", \""
                + name + "\", \""
                + enclosingMethod + "\", \""
                + enclosingClass + "\", "
                + uniqueNum + ");";
    }
}
