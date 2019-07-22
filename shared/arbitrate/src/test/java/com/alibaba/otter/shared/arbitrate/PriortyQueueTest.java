package com.alibaba.otter.shared.arbitrate;

import java.util.PriorityQueue;

/**
 * Created by huahua on 2019/6/15.
 */
public class PriortyQueueTest {

    public static void main(String[] args) {
        PriorityQueue<Integer> queue = new PriorityQueue<>();
        queue.offer(6);
        queue.offer(3);
        queue.offer(1);
        System.out.println(queue.poll());
        System.out.println(queue.remove());
        System.out.println(queue.peek());
    }
}
