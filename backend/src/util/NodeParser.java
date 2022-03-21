package util;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public final class NodeParser {

    public static String getEnclosingClass(Node node) {
        return node.findAncestor(ClassOrInterfaceDeclaration.class).isPresent() ?
                node.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() :
                null;
    }

    public static String getEnclosingMethod(MethodDeclaration md) {
        return md.getDeclarationAsString(true, true, true);
    }

    public static String getEnclosingMethod(Node node) {
        return node.findAncestor(MethodDeclaration.class).isPresent() ?
                node.findAncestor(MethodDeclaration.class).get().getDeclarationAsString(true, true, true) :
                null;
    }
}
