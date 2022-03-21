package annotation;

import java.util.Objects;

public class VariableScope {

    private String varName;
    private String enclosingMethod;
    private String enclosingClass;

    public VariableScope(String varName, String enclosingMethod, String enclosingClass) {
        this.varName = varName;
        this.enclosingMethod = enclosingMethod;
        this.enclosingClass = enclosingClass;
    }

    public String getEnclosingMethod() {
        return enclosingMethod;
    }

    public String getVarName() {
        return varName;
    }

    public String getEnclosingClass() {
        return enclosingClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableScope scope = (VariableScope) o;

        if (!Objects.equals(varName, scope.varName)) return false;
        if (!Objects.equals(enclosingMethod, scope.enclosingMethod))
            return false;
        return Objects.equals(enclosingClass, scope.enclosingClass);
    }

    @Override
    public int hashCode() {
        int result = varName != null ? varName.hashCode() : 0;
        result = 31 * result + (enclosingMethod != null ? enclosingMethod.hashCode() : 0);
        result = 31 * result + (enclosingClass != null ? enclosingClass.hashCode() : 0);
        return result;
    }
}
