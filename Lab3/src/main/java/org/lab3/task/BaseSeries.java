package org.lab3.task;

import java.io.Serial;

abstract class Series {
    @Serial
    private static final long serialVersionUID = 1L;
    protected int a0;
    protected int d;
    protected String name;

    public abstract int getNumber(int j);

    public abstract int getSum(int n);

    public String getName() {
        return name;
    }
}
