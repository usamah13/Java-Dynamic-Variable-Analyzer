package ast;

public class UniqueNumberGenerator {

    private static int count = 0;

    public static int generate() {
        return count++;
    }

}
