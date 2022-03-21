import annotation.Track;

public class MethodArgsTest {


    public static void main(String[] args) {
        MethodArgsTest mat = new MethodArgsTest();
        mat.methodArgs(5, "test");

        int[] j = new int[]{30, 31, 32};
        mat.arrayTest(j);
        int k[];
        k = null;
        k = new int[]{1, 2, 3};
        mat.arrayTest(k);
    }

    @Track(var = "arg1", nickname = "arg1")
    @Track(var = "arg2", nickname = "arg2")
    @Track(var = "a", nickname = "a")
    public void methodArgs(int arg1, String arg2) {
        int aaa = 10;
        if (arg1 < 5) {
            arg1 = 4;
        }
        arg1 = 10;
        if (arg1 > 5) {
            arg1 = 33;
        }

        A a = new A(5, new int[]{1, 2, 3}, new B());
        nestedMethod(a);
        nestedStaticMethod(a);

        A notTrackedA = new A(111, new int[]{111, 222, 333}, new B());
        nestedMethod(notTrackedA);

        methodArgFromOtherMethod(a, arg1);
    }

    @Track(var = "x", nickname = "x")
    private void arrayTest(int[] x) {
        x[0] = -1;
        x = new int[2];
    }

    private static void nestedStaticMethod(A a) {
        a.setNum(1);
    }

    private static void methodArgFromOtherMethod(A c, int d) {
        d = 8;

        c.num = 55;
        c.setArr(new int[]{50,150,200});
    }

    private void nestedMethod(A alias) {
        alias.num = 3;
        alias.arr = new int[]{9,10,11};
        alias.arr[0] = 1;
        alias.obj = null;
        alias.setNum(4);
        alias.setArr(new int[]{12,13,14});
        alias.setObj(new B());
        alias.obj.bInt = 8;
        alias.obj.setbInt(9);
        // nothing after this point should be logged
        alias = new A(-100, new int[]{-100, 200, 300}, new B());
        alias.num = -10000000;
    }

    private class A {
        private int num;
        int[] arr;

        public void setNum(int num2) {
            num = num2;
        }

        public void setArr(int[] arr) {
            this.arr = arr;
        }

        public void setObj(B obj) {
            this.obj = obj;
        }

        private B obj;

        public A(int num, int[] arr, B obj) {
            this.num = num;
            this.arr = arr;
            this.obj = obj;
        }
    }

    private static class B {

        protected int bInt = 5;

        public void setbInt(int bInt) {
            this.bInt = bInt;
        }
    }
}

