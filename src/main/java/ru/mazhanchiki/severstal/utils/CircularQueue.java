package ru.mazhanchiki.severstal.utils;

import java.util.ArrayList;

public class CircularQueue<T> {
    private final ArrayList<T> array;
    private int front; // Индекс элемента в начале очереди
    private int rear; // Индекс элемента в конце очереди

    public CircularQueue() {
        this.array = new ArrayList<T>();
        this.front = 0;
        this.rear = -1; // Изначально очередь пуста
    }

    private int capacity() {
        return this.array.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void enqueue(T item) {
        if (isEmpty()) {
            array.add(item);
            rear += 1;
            return;
        }
        rear = (rear + 1) % capacity();
        array.add(rear, item);
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        T dequeuedItem = array.get(front % capacity());
        front = (front + 1) % capacity();
        return dequeuedItem;
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return array.get(front);
    }

    public void remove(T item) {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }

        int index = array.indexOf(item);
        if (index == -1) {
            throw new IllegalStateException("Item not found");
        }
        array.remove(index);
    }

    public int size() {
        return this.array.size();
    }
}
