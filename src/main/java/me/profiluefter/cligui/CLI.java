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
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.profiluefter.cligui.annotations.CLIApplication;
import me.profiluefter.cligui.annotations.CLIOption;
import me.profiluefter.cligui.annotations.CLInject;

/**
 *
 * @author Fabian Gurtner (fabian@profiluefter.me)
 */
public class CLI {

    @SuppressWarnings("UseSpecificCatch")
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
            //Retrieve the annotation of the class
            final CLIOption optionAnnotation = option.getAnnotation(CLIOption.class);
            //Get a list of all parameters
            final Parameter[] parameters = option.getParameters();
            //Get a list of all annotations for every parameter
            final Annotation[][] annotations = option.getParameterAnnotations();
            //Transform above array to a map with <index, annotation>
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
                    ).collect(Collectors
                            .toMap(entry -> entry.getKey(), entry -> (CLInject) entry.getValue()));

            //Add the method to the GUI
            gui.addMenuItem(optionAnnotation.value(), () -> {
                //Parameter values will be stored here
                Object[] parameterValues = new Object[parameters.length];

                try {
                    injections.forEach((i, annotation) -> {
                        //Get type of currently processing parameter
                        Class<?> type = parameters[i].getType();

                        //Save if the type is primitive, so that it can be cast later
                        final boolean isPrimitive = type.isPrimitive();

                        //Replace primitive type with boxed version of it
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
                                case "short":
                                    type = Short.class;
                                    break;
                                default:
                                    System.err.println(type.getName());
                            }
                        }

                        //Read the actual value
                        final Object readValue = CLIReader.read(type, annotation.value());

                        if (isPrimitive) {
                            //Convert boxed value to unboxed value if replaced previously
                            switch (type.getName()) {
                                case "java.lang.Integer":
                                    parameterValues[i] = ((Number) readValue).intValue();
                                    break;
                                case "java.lang.Boolean":
                                    parameterValues[i] = ((Boolean) readValue).booleanValue();
                                    break;
                                case "java.lang.Character":
                                    parameterValues[i] = ((Character) readValue).charValue();
                                    break;
                                case "java.lang.Float":
                                    parameterValues[i] = ((Number) readValue).floatValue();
                                    break;
                                case "java.lang.Long":
                                    parameterValues[i] = ((Number) readValue).longValue();
                                    break;
                                case "java.lang.Double":
                                    parameterValues[i] = ((Number) readValue).doubleValue();
                                    break;
                                case "java.lang.Short":
                                    parameterValues[i] = ((Number) readValue).shortValue();
                                    break;
                                default:
                                    System.err.println(type.getName());
                            }
                        } else {
                            //Just cast the read value to the required parameter type
                            parameterValues[i] = (type.cast(readValue));
                        }
                    });
               
                    //Execute!
                    option.invoke(null, parameterValues);
                } catch (InvocationTargetException ex) {
                    handleError(ex.getTargetException());
                    return;
                } catch (Exception ex) {
                    handleError(ex);
                    return;
                }
                System.out.println(optionAnnotation.successMessage());
            });
        });

        gui.run();
    }

    private static void handleError(Throwable e) {
        switch (e.getClass().getName()) {
            case "java.lang.NumberFormatException":
            case "java.util.InputMismatchException":
                System.err.println("Ungültige Eingabe!");
                break;
            default:
                throw new RuntimeException("Ein Fehler ist aufgetreten: ", e);
        }
    }
}
