package ast;

import annotation.VariableScope;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import loader.CodeLoader;
import org.apache.commons.io.FileUtils;
import util.Formatter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String PROGRAM_FILE_NAME = "/SimpleTest_primitives.java";
    private static final String MODIFIED_FILES_DIRECTORY = "backend/test/modifiedast";
    private static final String INPUT_FILE_PATH = "backend/test" + PROGRAM_FILE_NAME;
    private static final String VARIABLE_LOGGER_FILE_PATH = "backend/src/ast/VariableLogger.java";
    private static final String LINE_INFO_FILE_PATH = "backend/src/ast/LineInfo.java";
    private static final String VARIABLE_REF_LOGGER_FILE_PATH = "backend/src/ast/VariableReferenceLogger.java";
    private static final String MODIFIED_FILES_PACKAGE_NAME = "modifiedast";
    private static final String MODIFIED_AST_FILE_PATH = MODIFIED_FILES_DIRECTORY + PROGRAM_FILE_NAME;
    private static final String MODIFIED_VARIABLE_LOGGER_FILE_PATH = MODIFIED_FILES_DIRECTORY + "/VariableLogger.java";
    private static final String MODIFIED_LINE_INFO_FILE_PATH = MODIFIED_FILES_DIRECTORY + "/LineInfo.java";
    private static final String MODIFIED_VARIABLE_REF_LOGGER_FILE_PATH = MODIFIED_FILES_DIRECTORY + "/VariableReferenceLogger.java";

    public static void main(String[] args) throws Exception {
        // get ast
        CompilationUnit cu = StaticJavaParser.parse(new File(INPUT_FILE_PATH));
        processProgram(cu);
    }
    
    public static String process(String program) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(program);
        return processProgram(cu);
    }

    public static String processProgram(CompilationUnit cu) throws Exception {
        // collect names/aliases of variables to track
        VoidVisitor<Map<VariableScope, String>> variableAnnotationCollector = new VariableAnnotationCollector();
        // map of variable scope -> the aliases we'll track them under
        Map<VariableScope, String> variablesToTrack = new HashMap<>();
        variableAnnotationCollector.visit(cu, variablesToTrack);
        // add logging code to ast
        ModifierVisitor<Map<VariableScope, List<LineInfo>>> variableHistoryModifier = new VariableHistoryModifier();
        // map of variable scope -> list of LineInfo for each line a mutation occurs
        Map<VariableScope, List<LineInfo>> lineInfoMap = new HashMap<>();
        // we add an entry for the first declaration of a variable to pass in the alias
        variablesToTrack.keySet().forEach(var ->
                lineInfoMap.put(var,
                        new ArrayList<>(List.of(new LineInfo(var.getVarName(), variablesToTrack.get(var))))));
        variableHistoryModifier.visit(cu, lineInfoMap);
        // this is super hacky, in order to get the alias info to the visit methods the first item in the list is a
        // LineInfo with only the name and alias. Since it's not a real LineInfo we delete it here. I'll fix this at
        // a later date
        lineInfoMap.values().forEach(statementList -> statementList.remove(0));
        // add a call to VariableLogger.writeOutputToDisk() to write output after execution is complete
        try {
            MethodDeclaration mainMethod = cu.findFirst(MethodDeclaration.class, methodDeclaration ->
                    methodDeclaration.getDeclarationAsString().contains("public static void main(String[] args)")).get();
            IOException e = new IOException();
            mainMethod.addThrownException(e.getClass());
            mainMethod.getBody().get().addStatement("VariableLogger.writeOutputToDisk();");
        } catch (NoSuchElementException e) {
            System.out.println("File does not contain a main method");
            System.exit(1);
        }

        List<String> classNames = new ArrayList<>();
        VoidVisitor<List<String>> classNameVisitor = new ClassNameCollector();
        classNameVisitor.visit(cu, classNames);
        String className = classNames.get(classNames.size()-1);

        writeModifiedProgram(cu, className);
        writeModifiedVariableLogger(lineInfoMap, variablesToTrack);
        writeModifiedVariableReferenceLogger();
        writeModifiedLineInfo();

        try {
            CodeLoader.run(className);
        } finally {
            try {
                File f = new File("backend/test/modifiedast");
                FileUtils.deleteDirectory(f);
                f.mkdirs();
                File f1 = new File("backend/test/modifiedast/gitProblem.txt");
                f1.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        String s =  new String(Files.readAllBytes(Paths.get("out/output.json")));
        File f = new File("out/output.json");
        f.delete();
        return s;
    }

    private static String populateLineInfoMap(Map<VariableScope, List<LineInfo>> lineInfoMap) {
        StringBuilder putStatements = new StringBuilder();
        for (List<LineInfo> lineInfos : lineInfoMap.values()) {
            for (LineInfo lineInfo : lineInfos) {
                putStatements.append(Formatter.generatePutStatement(lineInfo.getUniqueIdentifier(),
                        lineInfo.getName(),
                        lineInfo.getNickname(),
                        lineInfo.getType(),
                        lineInfo.getLineNum(),
                        lineInfo.getStatement(),
                        lineInfo.getEnclosingClass(),
                        lineInfo.getEnclosingMethod()
                ));
            }
        }
        return putStatements.toString();
    }

    private static void writeModifiedProgram(CompilationUnit cu, String classname) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MODIFIED_FILES_DIRECTORY + "/" + classname + ".java"));
        cu.setPackageDeclaration(MODIFIED_FILES_PACKAGE_NAME);
        cu.addImport("java.util.HashSet");
        cu.addImport("java.util.Set");
        writer.write(cu.toString());
        writer.close();
    }

    private static void writeModifiedVariableLogger(Map<VariableScope, List<LineInfo>> lineInfoMap,
                                                    Map<VariableScope, String> variablesToTrack) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MODIFIED_VARIABLE_LOGGER_FILE_PATH));
        BufferedReader reader = new BufferedReader(new FileReader(VARIABLE_LOGGER_FILE_PATH));
        String line;
        StringBuilder variableLoggerString = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.contains("package ast")) {
                line = "package " + MODIFIED_FILES_PACKAGE_NAME + ";";
            }
            variableLoggerString.append(line).append("\n");
            if (line.contains("public static Map<Integer, LineInfo> lineInfoMap = new HashMap<>() {{")) {
                // take lineInfoMap from above and stick it into lineInfoMap in VariableLogger
                variableLoggerString.append(populateLineInfoMap(lineInfoMap));
            }
            if (line.contains("private static Set<VariableScope> trackedScopes = new HashSet<>() {{")) {
                variableLoggerString.append(populateTrackedScopes(variablesToTrack));
            }
        }
        writer.write(variableLoggerString.toString());
        writer.close();
    }

    private static void writeModifiedLineInfo() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MODIFIED_LINE_INFO_FILE_PATH));
        CompilationUnit lineInfoCU = StaticJavaParser.parse(new File(LINE_INFO_FILE_PATH));
        lineInfoCU.setPackageDeclaration(MODIFIED_FILES_PACKAGE_NAME);
        writer.write(lineInfoCU.toString());
        writer.close();
    }

    private static void writeModifiedVariableReferenceLogger() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MODIFIED_VARIABLE_REF_LOGGER_FILE_PATH));
        CompilationUnit lineInfoCU = StaticJavaParser.parse(new File(VARIABLE_REF_LOGGER_FILE_PATH));
        lineInfoCU.setPackageDeclaration(MODIFIED_FILES_PACKAGE_NAME);
        writer.write(lineInfoCU.toString());
        writer.close();
    }

    private static String populateTrackedScopes(Map<VariableScope, String> variablesToTrack) {
        StringBuilder putStatements = new StringBuilder();
        for (VariableScope vs : variablesToTrack.keySet()) {
            putStatements.append(Formatter.addTrackedScope(vs));
        }
        return putStatements.toString();
    }
}