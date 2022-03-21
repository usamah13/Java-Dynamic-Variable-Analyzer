import annotation.Track;

public class LoopTest {

    public static void main(String[] args) {
        LoopTest mat = new LoopTest();
        mat.loopExample();
    }

    @Track(var = "i", nickname = "i")
    @Track(var = "j", nickname = "j")
    @Track(var = "k", nickname = "k")
    @Track(var = "l", nickname = "l")
    @Track(var = "a", nickname = "a")
    public void loopExample() {

        int b = 0;
        for (int i = 0; i < 3; i++) {
            b++;
        }

        int j = 1;
        for (j = 0; j < 3; j++) {
            b++;
        }

        int a = 0;
        while (a++ < 3) {
            b++;
        }

        int c = 0;
        do {
            b++;
        } while (a++ < 2);


    }

}