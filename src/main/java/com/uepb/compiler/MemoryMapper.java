package com.uepb.compiler;

public class MemoryMapper {
    private int currentAddress = 0;

    public int allocate() {
        return currentAddress++;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public void restore(int address) {
        currentAddress = address;
    }
}