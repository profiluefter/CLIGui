/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.profiluefter.cligui;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.profiluefter.cligui.annotations.CLIApplication;
import me.profiluefter.cligui.annotations.CLIOption;
import me.profiluefter.cligui.annotations.CLInject;

import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fabian Gurtner (fabian@profiluefter.me)
 */
public class CLI {

    public static void launch(Class clazz) {
        CLIGui gui;

        //Verify that the class has the required annotation
        assert clazz.getAnnotation(CLIApplication.class) != null;

        gui = new CLIGui(((CLIApplication) clazz.getAnnotation(CLIApplication.class)).value());

        //Get all methods that are annotated with CLIOption
        final List<Method> options
                = Arrays.stream(clazz.getMethods())
                        .filter(m -> m.isAnnotationPresent(CLIOption.class))
                        .collect(Collectors.toList());

        options.forEach((option) -> {
            final CLIOption optionAnnotation = option.getAnnotation(CLIOption.class);
            final Parameter[] parameters = option.getParameters();
            final Annotation[][] annotations = option.getParameterAnnotations();
            final Map<Integer, CLInject> injections = IntStream
                    .range(0, annotations.length)
                    .mapToObj(i -> new SimpleEntry<>(i, annotations[i]))
                    .filter(
                            entry -> Arrays.stream(entry.getValue())
                                    .anyMatch(annotation -> annotation
                                    .annotationType()
                                    .equals(CLInject.class)
                                    )
                    ).map(entry
                            -> new SimpleEntry<>(
                            entry.getKey(),
                            Arrays.stream(entry.getValue())
                                    .filter(e -> e.annotationType()
                                    .equals(CLInject.class))
                                    .findAny()
                                    .get()
                    )
                    ).collect(Collectors.toMap(entry -> entry.getKey(), entry -> (CLInject) entry.getValue()));

            gui.addMenuItem(optionAnnotation.value(), () -> {
                Object[] parameterValues = new Object[parameters.length];

                injections.forEach((i, annotation) -> {
                    Class<?> type = parameters[i].getType();

                    final boolean isPrimitive = type.isPrimitive();

                    if (isPrimitive) {
                        switch (type.getName()) {
                            case "int":
                                type = Integer.class;
                                break;
                            case "boolean":
                                type = Boolean.class;
                                break;
                            case "char":
                                type = Character.class;
                                break;
                            case "float":
                                type = Float.class;
                                break;
                            case "long":
                                type = Long.class;
                                break;
                            case "double":
                                type = Double.class;
                                break;
                            default:
                                System.err.println(type.getName());
                        }
                    }
                    final Object readValue = CLIReader.read(type, annotation.value());

                    if (isPrimitive) {
                        switch (type.getName()) {
                            case "int":
                                parameterValues[i] = ((Number)readValue).intValue();
                                break;
                            case "boolean":
                                parameterValues[i] = ((Boolean)readValue).booleanValue();
                                break;
                            case "char":
                                parameterValues[i] = ((Character)readValue).charValue();
                                break;
                            case "float":
                                parameterValues[i] = ((Number)readValue).floatValue();
                                break;
                            case "long":
                                parameterValues[i] = ((Number)readValue).longValue();
                                break;
                            case "double":
                                parameterValues[i] = ((Number)readValue).doubleValue();
                                break;
                            default:
                                System.err.println(type.getName());
                        }
                    } else {
                        parameterValues[i] = (type.cast(readValue));
                    }
                });
                
                Arrays.stream(parameterValues).map(Object::getClass).forEach(System.out::println);

                //TODO: FIX
                
                try {
                    option.invoke(null, parameterValues);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            });
        });

        gui.run();
    }
}
