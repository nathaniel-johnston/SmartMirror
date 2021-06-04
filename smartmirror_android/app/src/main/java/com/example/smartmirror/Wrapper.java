package com.example.smartmirror;

public class Wrapper<T> {
    private T val;

    public void setValue(T val) {
        this.val = val;
    }

    public T getValue() {
        return val;
    }
}
