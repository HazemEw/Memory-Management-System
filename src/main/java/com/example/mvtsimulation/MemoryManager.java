package com.example.mvtsimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class MemoryManager {
    private int totalMemory;
    private int osMemory;
    private int availableMemory;
    private List<Process> allocated;
    private List<int[]> freeHoles;

    public MemoryManager(int totalMemory, int osMemory) {
        this.totalMemory = totalMemory;
        this.osMemory = osMemory;
        this.availableMemory = totalMemory - osMemory;
        this.allocated = new ArrayList<>();
        this.freeHoles = new ArrayList<>();
        this.freeHoles.add(new int[]{osMemory, totalMemory - 1});
    }


    // Add Process to Memory
    public boolean allocateProcess(Process process) {
        for (int i = 0; i < freeHoles.size(); i++) {
            int[] hole = freeHoles.get(i);
            if (hole[1] - hole[0] + 1 >= process.size) {
                process.base = hole[0];
                process.limit = hole[0] + process.size - 1;
                allocated.add(process);

                if (hole[1] - hole[0] + 1 == process.size) {
                    freeHoles.remove(i);
                } else {
                    hole[0] += process.size;
                }
                return true;
            }
        }
        return false;
    }


    // Finish  Process
    public void deallocateProcess(Process process) {
        allocated.remove(process);
        freeHoles.add(new int[]{process.base, process.limit});
        freeHoles.sort(Comparator.comparingInt(a -> a[0]));

        List<int[]> mergedHoles = new ArrayList<>();
        for (int[] hole : freeHoles) {
            if (!mergedHoles.isEmpty() && mergedHoles.get(mergedHoles.size() - 1)[1] + 1 == hole[0]) {
                mergedHoles.get(mergedHoles.size() - 1)[1] = hole[1];
            } else {
                mergedHoles.add(hole);
            }
        }
        freeHoles = mergedHoles;
    }

    // Merge Free Holes
    public String compactMemory() {
        allocated.sort(Comparator.comparingInt(p -> p.base));
        int currentBase = osMemory;

        StringBuilder sb = new StringBuilder();

        List<String> oldFreeHolesInfo = new ArrayList<>();
        for (int[] hole : freeHoles) {
            oldFreeHolesInfo.add("Base: " + hole[0] + ", Limit: " + hole[1]);
        }

        for (Process process : allocated) {
            process.base = currentBase;
            process.limit = currentBase + process.size - 1;
            currentBase += process.size;
        }

        freeHoles.clear();
        freeHoles.add(new int[]{currentBase, totalMemory - 1});

        List<String> newFreeHolesInfo = new ArrayList<>();
        for (int[] hole : freeHoles) {
            newFreeHolesInfo.add("Base: " + hole[0] + ", Limit: " + hole[1]);
        }

        sb.append("Old Free Holes:\n");
        for (String info : oldFreeHolesInfo) {
            sb.append(info).append("\n");
        }

        sb.append("\nNew Free Hole:");
        for (String info : newFreeHolesInfo) {
            sb.append(info).append("\n");
        }

        return sb.toString();
    }


    // get All located process
    public List<Process> getAllocatedProcesses() {
        return allocated;
    }

    public List<int[]> getFreeHoles() {
        return freeHoles;
    }

    public List<MemoryNode> getMemoryNodes() {
        List<MemoryNode> memoryNodes = new ArrayList<>();
        for (Process process : allocated) {
            memoryNodes.add(new MemoryNode(process.base, process.limit, MemoryNodeType.PROCESS,process.processId, process.timeInMemory));
        }

        for (int[] hole : freeHoles) {
            memoryNodes.add(new MemoryNode(hole[0], hole[1], MemoryNodeType.FREE_HOLE,0,0));
        }

        memoryNodes.sort(Comparator.comparingInt(node -> node.base));

        return memoryNodes;
    }

    public String getMemoryContents() {
        StringBuilder sb = new StringBuilder();

        // OS Memory
        sb.append("--------------------------------------------------------------------------\n");
        sb.append("OS -> base: ").append(0).append(", limit: ").append(osMemory - 1).append("\n");

        List<MemoryNode> memoryNodes = getMemoryNodes();
        // Print memory nodes
        for (MemoryNode node : memoryNodes) {
            sb.append("--------------------------------------------------------------------------\n");
            sb.append(node.toString()).append("\n");
        }
        // End of Memory
        sb.append("--------------------------------------------------------------------------\n");
        return sb.toString();
    }
}
