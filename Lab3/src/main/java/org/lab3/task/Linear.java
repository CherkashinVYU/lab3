package org.lab3.task;

class Linear extends Series {
    public Linear(int a0, int d, String name) {
        this.a0 = a0;
        this.d = d;
        this.name = name;
    }

    @Override
    public int getNumber(int j) {
        int tmp = a0;
        for (int i = 1; i <= j; i++) {
            tmp += i * d;
        }
        return tmp;
    }

    @Override
    public int getSum(int n) {
        return (n + 1) * (a0 + getNumber(n)) / 2;
    }

    public int getA0() {
        return a0;
    }

    public int getD() {
        return d;
    }
}
