package com.example.kaplat.server;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Getter
public class StackController {
    private final Stack<Integer> stack = new Stack<>();

    public void pushToStack(int argumentToAdd) {
        this.stack.push(argumentToAdd);
    }

    public int popFromStack() {
        return this.stack.pop();
    }

    public int getStackSize() {
        return this.stack.size();
    }

    public void popAmountFromStack(int amount) {
        for (int i = 0 ; i < amount ; i++) {
            this.popFromStack();
        }
    }

    private ArrayList<Integer> getStackAsList() {
        List<Integer> list = this.stack.stream().toList();
        ArrayList arrayList = new ArrayList();
        for (int number: list) {
            arrayList.add(number);
        }
        return arrayList;
    }

    public ArrayList<Integer> getReverseStackAsList() {
        ArrayList<Integer> list = this.getStackAsList();
        Collections.reverse(list);
        return list;
    }
}
