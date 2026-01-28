package com.techx.tradex.common.utils;

import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.expression.Resolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class BeanUtils {
    private final static PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
    private final static ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();

    public static void copyProperties(Object orig, Object dest) {
        copyProperties(orig, dest, false);
    }

    public static void copyProperties(Object orig, Object dest, boolean ignoreNull) {

        // Validate existence of the specified beans
        if (dest == null) {
            throw new IllegalArgumentException
                    ("No destination bean specified");
        }
        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }
        // Copy the properties, converting as necessary
        try {
            if (orig instanceof DynaBean) {
                DynaProperty[] origDescriptors =
                        ((DynaBean) orig).getDynaClass().getDynaProperties();
                for (int i = 0; i < origDescriptors.length; i++) {
                    String name = origDescriptors[i].getName();
                    // Need to check isReadable() for WrapDynaBean
                    // (see Jira issue# BEANUTILS-61)
                    if (propertyUtilsBean.isReadable(orig, name) &&
                            propertyUtilsBean.isWriteable(dest, name)) {
                        Object value = ((DynaBean) orig).get(name);
                        if (value == null && ignoreNull) continue;
                        copyProperty(dest, name, value);
                    }
                }
            } else if (orig instanceof Map) {
                @SuppressWarnings("unchecked")
                // Map properties are always of type <String, Object>
                        Map<String, Object> propMap = (Map<String, Object>) orig;
                for (Map.Entry<String, Object> entry : propMap.entrySet()) {
                    String name = entry.getKey();
                    if (propertyUtilsBean.isWriteable(dest, name)) {
                        Object value = entry.getValue();
                        if (value == null && ignoreNull) continue;
                        copyProperty(dest, name, value);
                    }
                }
            } else /* if (orig is a standard JavaBean) */ {
                PropertyDescriptor[] origDescriptors =
                        propertyUtilsBean.getPropertyDescriptors(orig);
                for (int i = 0; i < origDescriptors.length; i++) {
                    String name = origDescriptors[i].getName();
                    if ("class".equals(name)) {
                        continue; // No point in trying to set an object's class
                    }
                    if (propertyUtilsBean.isReadable(orig, name) &&
                            propertyUtilsBean.isWriteable(dest, name)) {
                        try {
                            Object value = propertyUtilsBean.getSimpleProperty(orig, name);
                            if (value == null && ignoreNull) continue;
                            copyProperty(dest, name, value);
                        } catch (NoSuchMethodException e) {
                            // Should not happen
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            // hsould not happen
            e.printStackTrace();
        }
    }


    public static void copyProperty(Object bean, String name, Object value) {
        // Resolve any nested expression to get the actual target bean
        try {
            Object target = bean;
            Resolver resolver = propertyUtilsBean.getResolver();
            while (resolver.hasNested(name)) {
                try {
                    target = propertyUtilsBean.getProperty(target, resolver.next(name));
                    name = resolver.remove(name);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // should not happen here
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    return; // Skip this property setter
                }
            }

            // Declare local variables we will require
            String propName = resolver.getProperty(name); // Simple name of target property
            Class<?> type = null;                         // Java type of target property
            int index = resolver.getIndex(name);         // Indexed subscript value (if any)
            String key = resolver.getKey(name);           // Mapped key value (if any)

            // Calculate the target property type
            if (target instanceof DynaBean) {
                DynaClass dynaClass = ((DynaBean) target).getDynaClass();
                DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
                if (dynaProperty == null) {
                    return; // Skip this property setter
                }
                type = dynaPropertyType(dynaProperty, value);
            } else {
                PropertyDescriptor descriptor = null;
                try {
                    descriptor =
                            propertyUtilsBean.getPropertyDescriptor(target, name);
                    if (descriptor == null) {
                        return; // Skip this property setter
                    }
                } catch (NoSuchMethodException e) {
                    return; // Skip this property setter
                }
                type = descriptor.getPropertyType();
                if (type == null) {
                    return;
                }
            }

            // Convert the specified value to the required type and store it
            if (index >= 0) {                    // Destination must be indexed
                value = convertForCopy(value, type.getComponentType());
                try {
                    propertyUtilsBean.setIndexedProperty(target, propName,
                            index, value);
                } catch (NoSuchMethodException e) {
                    throw new InvocationTargetException
                            (e, "Cannot set " + propName);
                }
            } else if (key != null) {            // Destination must be mapped
                // Maps do not know what the preferred data type is,
                // so perform no conversions at all
                // FIXME - should we create or support a TypedMap?
                try {
                    propertyUtilsBean.setMappedProperty(target, propName,
                            key, value);
                } catch (NoSuchMethodException e) {
                    throw new InvocationTargetException
                            (e, "Cannot set " + propName);
                }
            } else {                             // Destination must be simple
                value = convertForCopy(value, type);
                try {
                    propertyUtilsBean.setSimpleProperty(target, propName, value);
                } catch (NoSuchMethodException e) {
                    throw new InvocationTargetException
                            (e, "Cannot set " + propName);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    /**
     * Determines the type of a {@code DynaProperty}. Here a special treatment
     * is needed for mapped properties.
     *
     * @param dynaProperty the property descriptor
     * @param value        the value object to be set for this property
     * @return the type of this property
     */
    private static Class<?> dynaPropertyType(DynaProperty dynaProperty,
                                             Object value) {
        if (!dynaProperty.isMapped()) {
            return dynaProperty.getType();
        }
        return (value == null) ? String.class : value.getClass();
    }

    /**
     * Performs a type conversion of a property value before it is copied to a target
     * bean. This method delegates to {@link #convert(Object, Class)}, but <b>null</b>
     * values are not converted. This causes <b>null</b> values to be copied verbatim.
     *
     * @param value the value to be converted and copied
     * @param type  the target type of the conversion
     * @return the converted value
     */
    private static Object convertForCopy(Object value, Class<?> type) {
        return (value != null) ? convert(value, type) : value;
    }

    /**
     * <p>Convert the value to an object of the specified class (if
     * possible).</p>
     *
     * @param value Value to be converted (may be null)
     * @param type  Class of the value to be converted to
     * @return The converted value
     * @throws ConversionException if thrown by an underlying Converter
     * @since 1.8.0
     */
    protected static Object convert(Object value, Class<?> type) {
        Converter converter = convertUtilsBean.lookup(type);
        if (converter != null) {
            return converter.convert(type, value);
        } else {
            return value;
        }
    }
}
