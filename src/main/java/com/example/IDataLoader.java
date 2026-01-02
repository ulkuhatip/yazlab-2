package com.example;

public interface IDataLoader {
    void load(String path, Graph graph);
    void save(String path, Graph graph);
}