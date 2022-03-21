import annotation.Track;

public class ArrayTest {

    public static int ONE_BILLION = 1000000000;

    private double memory = 0;

    public static void main(String[] args) {
        ArrayTest st = new ArrayTest();
        st.calc();
        st.helloWorld();
        st.helloWorldNoDec();
    }

    @Track(var="x", nickname ="x")
    @Track(var = "y", nickname = "y")
    @Track(var = "a", nickname = "a")
    public Double calc() {
        double a = -1;
        a = 4;

        int x[];
        x = null;
        x = new int[]{1, 2, 3};
        int[] y = x;
        int[] z = y;
        x = y;
        y[0] = 200;
        x[1] = 300;
        z[2] = 400;
        x = new int[]{4, 5, 6};
        y = new int[]{7, 8, 9};
        nestedMethod(x);
        return a;
    }

    private void nestedMethod(int[] alias) {
        alias[0] = -1;
        alias = new int[2];
    }

    @Track(var="m", nickname="m")
    @Track(var="i", nickname="i")
    public void helloWorld() {
        int m = 100;
        for (int i = 0; i < 3; i++) {
            m++;
        }
    }

    @Track(var="i", nickname="i")
    public void helloWorldNoDec() {
        int j = 3;
        for (int i= 100; i < 103; i++) {
            j = j + i;
        }
    }
}
