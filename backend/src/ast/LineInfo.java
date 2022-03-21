package ast;

public class LineInfo {

    private String name, nickname, type, statement, enclosingClass, enclosingMethod;
    private Integer lineNum, uniqueIdentifier;

    public LineInfo(String name, String nickname, String type, Integer lineNum, String statement, String enclosingClass,
                    String enclosingMethod, int uniqueIdentifier) {
        this.name = name;
        this.nickname = nickname;
        this.type = type;
        this.lineNum = lineNum;
        this.statement = statement;
        this.enclosingClass = enclosingClass;
        this.enclosingMethod = enclosingMethod;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public LineInfo(String name, String alias) {
        this.name = name;
        this.nickname = alias;
    }

    public LineInfo() {
        // dummy constructor
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getEnclosingClass() {
        return enclosingClass;
    }

    public void setEnclosingClass(String enclosingClass) {
        this.enclosingClass = enclosingClass;
    }

    public String getEnclosingMethod() {
        return enclosingMethod;
    }

    public void setEnclosingMethod(String enclosingMethod) {
        this.enclosingMethod = enclosingMethod;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(int uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }
}
