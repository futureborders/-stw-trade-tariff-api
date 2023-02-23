// Copyright 2021 Crown Copyright (Single Trade Window)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.util;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Field;
import java.util.Set;

public class ReflectionUtils {
    public static <PARENT_CLASS> Set<PARENT_CLASS> getFieldsOfType(Object object, Class<PARENT_CLASS> classOfFields) {
        return ImmutableSet.copyOf(object.getClass().getDeclaredFields()).stream()
                .filter(field -> classOfFields.isAssignableFrom(field.getType()))
                .map(field -> getNonNullFieldValue(object, field))
                .map(classOfFields::cast)
                .collect(toSet());
    }

    private static Object getNonNullFieldValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                throw new IllegalArgumentException("Field: " + field.getName() + " is null");
            }
            return value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
