package ast;

import annotation.VariableScope;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import util.NodeParser;
import util.Scoper;
import util.StatementCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableHistoryModifier extends ModifierVisitor<Map<VariableScope, List<LineInfo>>> {

    private Map<VariableScope, List<LineInfo>> originalLineInfoMap = null;

    private void checkOriginalLineInfo(Map<VariableScope, List<LineInfo>> original) {
        if (originalLineInfoMap == null) {
            originalLineInfoMap = new HashMap<>();
            for (Map.Entry<VariableScope, List<LineInfo>> entry : original.entrySet()) {
                originalLineInfoMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return;
    }

    @Override
    public VariableDeclarationExpr visit(VariableDeclarationExpr vde, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(vde, lineInfoMap);
        for (VariableDeclarator vd : vde.getVariables()) {
            String name = vd.getNameAsString();
            VariableScope scope = Scoper.createScope(name, vd);
            if (!isTrackedVariable(scope, lineInfoMap)) {
                continue;
            }
            Statement nodeContainingEntireStatement = (Statement) vd.getParentNode().get().getParentNode().get();
            String type = vd.getType().toString();
            int id = UniqueNumberGenerator.generate();
            if (isDeclaredButNotInitialized(vd)) {
                injectCodeOnNextLine(nodeContainingEntireStatement, vd,
                        StatementCreator.evaluateVarDeclarationWithoutInitializerStatement(name,
                                scope.getEnclosingMethod(), scope.getEnclosingClass(), id));
            } else if (nodeContainingEntireStatement instanceof ForStmt) {
                injectCodeOnNextLine(nodeContainingEntireStatement, vd,
                        StatementCreator.evaluateForLoopVarDeclarationStatement(name, scope.getEnclosingMethod(),
                                scope.getEnclosingClass(), id));
            } else {
                injectCodeOnNextLine(nodeContainingEntireStatement, vd,
                        StatementCreator.evaluateVarDeclarationStatement(name, scope.getEnclosingMethod(),
                                scope.getEnclosingClass(), id));
            }
            addToLineInfoMap(scope, type, nodeContainingEntireStatement.toString(), vd, lineInfoMap, id);
        }
        return vde;
    }

    @Override
    public FieldDeclaration visit(FieldDeclaration fd, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(fd, lineInfoMap);
        // we set all fields to public access so that VariableReferenceLogger.checkBaseAndNestedObjects() can access
        // each field
        setAccessToPublic(fd);
        return fd;
    }

    @Override
    public AssignExpr visit(AssignExpr ae, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(ae, lineInfoMap);
        // If making an array access assignment we want the name of the variable without the square brackets
        String name = (isArrayAccessAssignment(ae)) ?
                ((ArrayAccessExpr) ae.getTarget()).toString().split("\\[")[0] :
                ae.getTarget().toString();
        VariableScope scope = Scoper.createScope(name, ae);
        Statement nodeContainingEntireStatement = (Statement) ae.getParentNode().get();
        trackVariableMutation(name, scope, nodeContainingEntireStatement, ae, lineInfoMap);
        return ae;
    }

    @Override
    public UnaryExpr visit(UnaryExpr ue, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(ue, lineInfoMap);
        String name = ue.getExpression().toString();
        VariableScope scope = Scoper.createScope(name, ue);
        if (isTrackedVariable(scope, lineInfoMap)) {
            Node nodeContainingEntireStatement = ue.getParentNode().get();
            while (nodeContainingEntireStatement instanceof BinaryExpr be) {
                nodeContainingEntireStatement = be.getParentNode().get();
            }
            trackVariableMutation(name, scope, (Statement) nodeContainingEntireStatement, ue, lineInfoMap);
        }
        return ue;
    }

    @Override
    public MethodDeclaration visit(MethodDeclaration md, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(md, lineInfoMap);
        for (Parameter p : md.getParameters()) {
            String name = p.getNameAsString();
            VariableScope scope = Scoper.createScope(name, p);
            if (!isTrackedVariable(scope, originalLineInfoMap)) {
                continue;
            }
            String type = p.getType().toString();
            int id = UniqueNumberGenerator.generate();
            Statement body = md.findAll(BlockStmt.class).get(0);
            injectCodeOnNextLine(body, md, StatementCreator.evaluateVarDeclarationStatement(name,
                    scope.getEnclosingMethod(), scope.getEnclosingClass(), id));
            addToLineInfoMap(scope, type, md.getDeclarationAsString(),
                    body, lineInfoMap, id);
        }

        return md;
    }

    @Override
    public MethodCallExpr visit(MethodCallExpr mce, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        checkOriginalLineInfo(lineInfoMap);
        super.visit(mce, lineInfoMap);
        if (mce.getScope().isPresent()) {
            NameExpr exprScope = (NameExpr) getBaseScope(mce.getScope().get());
            String name = exprScope.getNameAsString();
            // so we don't track system and similar method calls (e.g. System.out.println(...))
            if (isJavaDefaultStaticObject(name)) {
                return mce;
            }
            VariableScope scope = Scoper.createScope(name, mce);
            Statement nodeContainingEntireStatement = (Statement) mce.getParentNode().get();
            int id = UniqueNumberGenerator.generate();
            addToLineInfoMap(scope, null, nodeContainingEntireStatement.toString(), mce, lineInfoMap, id);
            Statement injectedLine = StatementCreator.checkBaseAndNestedObjectsStatement(name,
                    scope.getEnclosingMethod(), scope.getEnclosingClass(), id);
            injectCodeOnNextLine(nodeContainingEntireStatement, mce, injectedLine);
        }
        return mce;
    }

    private boolean isJavaDefaultStaticObject(String name) {
       return name != null &&
               name.length() > 0 &&
               Character.isUpperCase(name.charAt(0));
    }

    private Expression getBaseScope(Expression scope) {
        if (scope instanceof FieldAccessExpr scopeAsFAE) {
            return getBaseScope(scopeAsFAE.getScope());
        }
        return scope;
    }

    private void trackVariableMutation(String name, VariableScope scope, Statement nodeContainingEntireStatement,
                                       Node node, Map<VariableScope,
            List<LineInfo>> lineInfoMap) {
        int id = UniqueNumberGenerator.generate();
        addToLineInfoMap(scope, null, nodeContainingEntireStatement.toString(), node, lineInfoMap, id);
        String[] subObjects = name.split("\\.");
        String objName = subObjects[0];
        Statement injectedLine = StatementCreator.evaluateAssignmentStatement(objName, scope.getEnclosingMethod(),
                scope.getEnclosingClass(), id);
        injectCodeOnNextLine(nodeContainingEntireStatement, node, injectedLine);
        for (int i = 1; i < subObjects.length; i++) {
            objName = objName + "." + subObjects[i];
            injectedLine = StatementCreator.checkBaseAndNestedObjectsStatement(objName, scope.getEnclosingMethod(),
                    scope.getEnclosingClass(), id);
            injectCodeOnNextLine(nodeContainingEntireStatement, node, injectedLine);
        }
        MethodDeclaration enclosingMethod = node.findAncestor(MethodDeclaration.class).isPresent() ?
                node.findAncestor(MethodDeclaration.class).get() : null;
        boolean isEnclosedByConstructor = node.findAncestor(ConstructorDeclaration.class).isPresent();
        if (isEnclosedByConstructor ||
                (enclosingMethod != null && !enclosingMethod.isStatic())) {
            injectedLine = StatementCreator.evaluateAssignmentStatement("this", scope.getEnclosingMethod(),
                    scope.getEnclosingClass(), id);
            injectCodeOnNextLine(nodeContainingEntireStatement, node, injectedLine);
        }
        if (node instanceof AssignExpr && nodeContainingEntireStatement instanceof ForStmt fs) {
            Statement injectOutsideOfLoop = StatementCreator.evaluateAssignmentStatement(objName, scope.getEnclosingMethod(),
                    scope.getEnclosingClass(), id);
            node.findAncestor(BlockStmt.class)
                    .ifPresent(block -> block.addStatement(1 + block.getStatements().indexOf(nodeContainingEntireStatement),
                            injectOutsideOfLoop));
        }
    }

    private void addToLineInfoMap(VariableScope scope, String type,
                                  String nodeContainingEntireStatement, Node node,
                                  Map<VariableScope, List<LineInfo>> lineInfoMap, int id) {
        String nickname = isTrackedVariable(scope, lineInfoMap) ? lineInfoMap.get(scope).get(0).getNickname() : null;
        Integer lineNum = getLineNum(node);
        String enclosingClass = NodeParser.getEnclosingClass(node);
        String enclosingMethod = NodeParser.getEnclosingMethod(node);
        if (!isTrackedVariable(scope, lineInfoMap)) {
            // add with a dummy item to make nickname hack work
            lineInfoMap.put(scope, new ArrayList<>(List.of(new LineInfo())));
        }
        lineInfoMap.get(scope).add(new LineInfo(scope.getVarName(), nickname, type, lineNum,
                nodeContainingEntireStatement,
                enclosingClass, enclosingMethod, id));
    }

    private Integer getLineNum(Node node) {
        return node.getBegin().isPresent() ? node.getBegin().get().line : null;
    }

    private void injectCodeOnNextLine(Statement anchorStatement, Node node, Statement loggingStatement) {

        if (node instanceof MethodDeclaration md && anchorStatement instanceof BlockStmt body) {
            // Add logging for method arguments
            body.addStatement(0, loggingStatement);
        } else if (anchorStatement instanceof ForStmt forStmt) {
            // If it's a for statement, don't include variable declaration (will be reclared each loop)
            if (forStmt.getBody() instanceof BlockStmt body) {
                body.addStatement(0, loggingStatement);
            }
        } else if (anchorStatement instanceof WhileStmt whileStmt) {
            // If it's a for statement, don't include variable declaration (will be reclared each loop)
            if (whileStmt.getBody() instanceof BlockStmt body) {
                body.addStatement(0, loggingStatement);
            }
        } else if (node.findAncestor(SwitchEntry.class).isPresent()) {
            NodeList<Statement> switchBlockStatements = node.findAncestor(SwitchEntry.class).get().getStatements();
            switchBlockStatements.add(switchBlockStatements.indexOf(anchorStatement) + 1, loggingStatement);
        } else {
            node.findAncestor(BlockStmt.class)
                    .ifPresent(block -> block.addStatement(1 + block.getStatements().indexOf(anchorStatement),
                            loggingStatement));
        }
    }

    private boolean isArrayAccessAssignment(AssignExpr ae) {
        return ae.getTarget() instanceof ArrayAccessExpr;
    }

    private boolean isTrackedVariable(VariableScope scope, Map<VariableScope, List<LineInfo>> lineInfoMap) {
        return lineInfoMap.containsKey(scope);
    }

    private boolean isDeclaredButNotInitialized(VariableDeclarator vd) {
        return vd.getInitializer().isEmpty();
    }

    private void setAccessToPublic(FieldDeclaration fd) {
        NodeList<Modifier> modifiers = fd.getModifiers();
        for (Modifier modifier : modifiers) {
            if (isAccessSpecifier(modifier)) {
                modifiers.replace(modifier, Modifier.publicModifier());
                return;
            }
        }
        modifiers.add(0, Modifier.publicModifier());
        fd.setModifiers(modifiers);
    }

    private boolean isAccessSpecifier(Modifier modifier) {
        return modifier.getKeyword() == Modifier.Keyword.PRIVATE ||
                modifier.getKeyword() == Modifier.Keyword.PROTECTED ||
                modifier.getKeyword() == Modifier.Keyword.PUBLIC;
    }
}
