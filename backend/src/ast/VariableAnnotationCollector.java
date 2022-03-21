package ast;

import annotation.VariableScope;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import util.NodeParser;

import java.util.Map;

public class VariableAnnotationCollector extends VoidVisitorAdapter<Map<VariableScope, String>> {

    @Override
    public void visit(MethodDeclaration md, Map<VariableScope, String> collector) {
        super.visit(md, collector);
        String enclosingMethod = NodeParser.getEnclosingMethod(md);
        String enclosingClass = NodeParser.getEnclosingClass(md);
        for (AnnotationExpr nae : md.getAnnotations()) {
            String name = nae.asNormalAnnotationExpr().getPairs().getFirst().get().getValue().toString().replace(
                    "\"", "");
            String nickname =
                    nae.asNormalAnnotationExpr().getPairs().getLast().get().getValue().toString().replace(
                            "\"", "");
            collector.put(new VariableScope(name, enclosingMethod, enclosingClass), nickname);
        }
    }
}