package ast;

import annotation.VariableScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VariableReferenceLogger {

    // maps obj reference -> set of variable scopes which track this reference
    public static HashMap<String, Set<VariableScope>> refToVarMap = new HashMap<>();
    // maps variable name -> the obj reference that the variable points to
    private static HashMap<VariableScope, String> varToRefMap = new HashMap<>();
    // maps obj reference -> json representation of object after last time it was mutated
    private static HashMap<String, String> refToJsonMap = new HashMap<>();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();


    public static void evaluateVarDeclaration(Object var, String varName, String enclosingMethod,
                                              String enclosingClass, int lineInfoNum) {
        VariableScope scope = new VariableScope(varName, enclosingMethod, enclosingClass);
        if (var == null) {
            varToRefMap.put(scope, null);
            VariableLogger.log(null, varName, enclosingMethod, enclosingClass, lineInfoNum);
            return;
        }
        // check if this reference already has an entry, it may if another tracked variable also references it
        if (!isTrackedReference(var.toString())) {
            refToVarMap.put(var.toString(), new HashSet<>());
        }
        refToVarMap.get(var.toString()).add(scope); // add this var to the list of vars that point to its reference
        varToRefMap.put(scope, var.toString());     // add an entry for this variable
        refToJsonMap.put(var.toString(), gson.toJson(var));
        VariableLogger.log(var, varName, enclosingMethod, enclosingClass, lineInfoNum);
    }

    public static void evaluateVarDeclarationWithoutInitializer(String varName, String enclosingMethod,
                                                                String enclosingClass, int lineInfoNum) {
        VariableScope scope = new VariableScope(varName, enclosingMethod, enclosingClass);
        varToRefMap.put(scope, null);
        VariableLogger.log("uninitialized", varName, enclosingMethod, enclosingClass, lineInfoNum);
    }

    public static void evaluateForLoopVarDeclaration(Object var, String varName, String enclosingMethod,
                                                     String enclosingClass, int lineInfoNum) {
        VariableScope scope = new VariableScope(varName, enclosingMethod, enclosingClass);
        // only add for loop variable to VarMap if it isn't already there
        if (!isTrackedReference(var.toString())) {
            refToVarMap.put(var.toString(), new HashSet<>());
            refToVarMap.get(var.toString()).add(scope); // add this var to the list of vars that point to its reference
            varToRefMap.put(scope, var.toString());     // add an entry for this variable
            VariableLogger.log(var, varName, enclosingMethod, enclosingClass, lineInfoNum);
        }
    }

    public static void evaluateAssignment(Object var, String varName, String enclosingMethod,
                                          String enclosingClass, int lineInfoNum) {
        VariableScope scope = new VariableScope(varName, enclosingMethod, enclosingClass);
        if (var == null) {
            evaluateNullAssignment(scope, lineInfoNum);
            return;
        }
        if (trackedVarReferenceHasChanged(var, scope)) { // if a tracked var now points to a different ref
            updateMapsWithNewReference(var, scope);      // update the map to reflect that
            // log for this one tracked var
            VariableLogger.log(var, varName, enclosingMethod, enclosingClass, lineInfoNum);
        }
        // if the above if block ran then checkBaseAndNestedObjects won't detect any changes since the
        // refToJsonMap entry for this ref was just updated to be this exact version of the obj
        checkBaseAndNestedObjects(var, varName, enclosingMethod, enclosingClass, lineInfoNum);
    }

    public static void checkBaseAndNestedObjects(Object baseObject, String varName, String enclosingMethod,
                                                 String enclosingClass, int lineInfoNum) {
        VariableScope scope = new VariableScope(varName, enclosingMethod, enclosingClass);
        if (baseObject == null) {
            return;
        }
        checkObject(baseObject, lineInfoNum, baseObject);
        for (Field field : baseObject.getClass().getFields()) {
            try {
                Object nestedObject = field.get(baseObject);
                if (nestedObject == null) {
                    continue;
                }
                checkObject(baseObject, lineInfoNum, nestedObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkObject(Object var, int lineInfoNum, Object nestedObject) {
        if (isTrackedReference(nestedObject.toString()) && isObjectModified(nestedObject)) {
            refToJsonMap.put(var.toString(), gson.toJson(var)); // update the refToJsonMap with the modified object
            logForAllVariablesThatPointToReference(var, lineInfoNum);
        }
    }

    private static void logForAllVariablesThatPointToReference(Object var, int lineInfoNum) {
        for (VariableScope trackedVariableScope : refToVarMap.get(var.toString())) {
            VariableLogger.log(var, trackedVariableScope.getVarName(), trackedVariableScope.getEnclosingMethod(),
                    trackedVariableScope.getEnclosingClass(),
                    lineInfoNum);
        }
    }

    private static boolean isObjectModified(Object var) {
        return refToJsonMap.get(var.toString()) != null &&
                !refToJsonMap.get(var.toString()).equals(gson.toJson(var));
    }

    private static boolean trackedVarReferenceHasChanged(Object var, VariableScope scope) {
        return varToRefMap.containsKey(scope) && !var.toString().equals(varToRefMap.get(scope));
    }

    private static void evaluateNullAssignment(VariableScope scope, int lineInfoNum) {
        if (varToRefMap.containsKey(scope)) {
            String oldRef = varToRefMap.get(scope);
            removeOldRefEntriesFromMap(scope, oldRef);
            varToRefMap.put(scope, null);
            // we don't add a null entry for refToVarMap here because this will lead to every variable declared as null
            // getting a history entry every time a tracked variable is set to null
            VariableLogger.log(null, scope.getVarName(), scope.getEnclosingMethod(), scope.getEnclosingClass(),
                    lineInfoNum);
        }
    }

    private static boolean isTrackedReference(String s) {
        return refToVarMap.containsKey(s);
    }

    private static void updateMapsWithNewReference(Object var, VariableScope scope) {
        String oldRef = varToRefMap.get(scope);              // grab a copy of varName's old obj ref
        String newRef = var.toString();
        removeOldRefEntriesFromMap(scope, oldRef);
        varToRefMap.put(scope, newRef);                      // replace varName's old entry with the new obj ref
        if (!isTrackedReference(newRef)) {                   // if this var ref doesn't have an entry
            refToVarMap.put(newRef, new HashSet<>());        // add an entry for the new ref
            refToJsonMap.put(var.toString(), gson.toJson(var));
        }
        refToVarMap.get(newRef).add(scope);                 // add varName to the list of variables that point to newRef
    }

    private static void removeOldRefEntriesFromMap(VariableScope scope, String oldRef) {
        if (oldRef != null) {
            refToVarMap.get(oldRef).remove(scope);     // remove varName from the list of variables that point to oldRef
            if (refToVarMap.get(oldRef).isEmpty()) {   // if no other variables we care about point to oldRef
                refToVarMap.remove(oldRef);            // remove oldRef from both maps that key by reference
                refToJsonMap.remove(oldRef);
                // we don't delete the varToRefMap entry because it just gets overwritten
            }
        }
    }
}
