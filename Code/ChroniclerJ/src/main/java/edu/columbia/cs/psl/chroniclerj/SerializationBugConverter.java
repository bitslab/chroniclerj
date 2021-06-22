package edu.columbia.cs.psl.chroniclerj;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * XStream uses a sequence of converters to turn objects into XML
 * ReflectionConverter is used as a last-resort, for non-serializable objects
 * It uses reflection to iterate over each field and turn it into XML
 * This converter will force to use reflection for classes known to have serialization bugs (e.g., NPE on writeObject)
 */

public class SerializationBugConverter extends ReflectionConverter {
    private static final String[] KNOWN_BUGGY_CLASSES = new String[] {
            "java.awt.color.ICC_ProfileRGB",
            "java.util.Collections$SynchronizedSet",
    };

    private static final Set<String> KNOWN_BUGGY_CLASSES_SET = new HashSet<>(Arrays.asList(KNOWN_BUGGY_CLASSES));

    public SerializationBugConverter(XStream xstream) {
        super(xstream.getMapper(), xstream.getReflectionProvider());
    }

    @Override
    public boolean canConvert(Class type) {
        return KNOWN_BUGGY_CLASSES_SET.contains(type.getName());
    }
}
