package com.trenska.longwang.controller;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class IndexController {
    public static void main(String[] args) {
//        NumberFormat numberInstance = NumberFormat.getNumberInstance();
//        numberInstance.setMaximumFractionDigits(2);
//        System.out.println(numberInstance.format(1.2345));

        List<String> s1 = Arrays.asList("study", "day", "foreach", "interesting", "bainian");
        List<String> s2 = Arrays.asList("study", "day", "foreach", "interesting", "bainian");
        Stream.Builder<Object> add = Stream.builder().add(s1).add(s2);
        add.andThen(System.out::print);

        System.out.println(s1.stream().reduce((s11, s12) -> s11.length() > s12.length() ? s11 : s12).get());
        System.out.println(s1.stream().mapToInt(String::length).sum());
        s1.stream().filter(s -> s.length() > 6).forEach(System.out::println);
        s1.stream().sorted(Comparator.naturalOrder()).forEach(System.out::println);
    }
}
