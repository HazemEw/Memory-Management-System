package com.example.mvtsimulation;

class MemoryNode {
    int base;
    int limit;
    int id;
    MemoryNodeType type;
    int time;

    public MemoryNode(int base, int limit, MemoryNodeType type,int name,int time) {
        this.base = base;
        this.limit = limit;
        this.type = type;
        this.id = name;
        this.time=time;
    }

    @Override
    public String toString() {
        if (type == MemoryNodeType.PROCESS) {
            return "Process " + id + " -> base: " + base + ", limit: " + limit + " , Time remaining in Memory " + time;
        } else {
            return "Free Hole -> base: " + base + ", limit: " + limit;
        }
    }
}