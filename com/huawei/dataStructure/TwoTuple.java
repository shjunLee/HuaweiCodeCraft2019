package com.huawei.dataStructure;

public class TwoTuple<A, B> {
    private A first;
    private B second;
    public TwoTuple(A a, B b) {
        first = a;
        second = b;
    }
    public A getFirst(){
        return first;
    }

    public B getSecond(){
        return second;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
