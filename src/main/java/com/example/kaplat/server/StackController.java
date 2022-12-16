package com.example.kaplat.server;

import com.example.kaplat.server.enums.OperationEnum;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Stack;

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
}
