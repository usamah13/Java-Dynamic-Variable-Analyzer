package util;

import annotation.VariableScope;

public final class Formatter {

    public static String generatePutStatement(int id, String name, String alias, String type, int lineNum,
                                              String statement, String enclosingClass, String enclosingMethod) {
        String parsedStatement = parseStatement(statement);
        return "\t\tput("
                + id + ", new LineInfo(\""
                + name + "\", \""
                + alias + "\", \""
                + type + "\","
                + lineNum + ", \""
                + parsedStatement + "\", \""
                + enclosingClass + "\", \""
                + enclosingMethod + "\", "
                + id + "));" + "\n";
    }

    public static String addTrackedScope(VariableScope vs) {
        return "\t\tadd("
                + "new VariableScope(\""
                + vs.getVarName() + "\", \""
                + vs.getEnclosingMethod() + "\", \""
                + vs.getEnclosingClass() + "\"));" + "\n";
    }
    private static String parseStatement(String statement) {
        String escapedStatement = statement
                .replaceAll("\n", "\\\\n")
                .replaceAll("\r", "\\\\r")
                .replaceAll("\"", "\\\\\"");
        String[] parsed = escapedStatement.split("\\\\n");
        StringBuilder sb = new StringBuilder();
        for (String s : parsed) {
            if (!containsLoggingStatements(s)) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private static boolean containsLoggingStatements(String statement) {
        return statement.contains("VariableReferenceLogger") || statement.contains("VariableLogger");
    }
}
