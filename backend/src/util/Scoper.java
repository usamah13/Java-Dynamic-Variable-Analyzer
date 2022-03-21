package util;

import annotation.VariableScope;
import com.github.javaparser.ast.Node;

public final class Scoper {
    public static VariableScope createScope(String name, Node node) {
        return new VariableScope(name, NodeParser.getEnclosingMethod(node), NodeParser.getEnclosingClass(node));
    }
}
