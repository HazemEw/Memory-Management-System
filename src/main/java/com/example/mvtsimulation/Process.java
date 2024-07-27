package com.example.mvtsimulation;

class Process {
    int processId;
    int size;
    int timeInMemory;
    int base;
    int limit;

    public Process(int processId, int size, int timeInMemory) {
        this.processId = processId;
        this.size = size;
        this.timeInMemory = timeInMemory;
        this.base = -1;
        this.limit = -1;
    }
}
