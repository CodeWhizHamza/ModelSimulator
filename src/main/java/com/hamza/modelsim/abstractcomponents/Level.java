package com.hamza.modelsim.abstractcomponents;

import java.util.HashMap;

public class Level {
    public String name;
    public HashMap<String, Integer[]> inputs;
    public HashMap<String, Integer[]> outputs;
    public HashMap<String, String[]> availableGates;
    public int maxGates;
    public int maxTime;
    public int previousScore;
    public boolean isLocked;
    public String hint;
    public String description;
}
